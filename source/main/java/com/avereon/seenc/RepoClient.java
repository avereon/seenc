package com.avereon.seenc;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefNotAdvertisedException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class RepoClient {

    private static final Logger log = LoggerFactory.getLogger(RepoClient.class);

    private final RepoClientConfig config;

    private final CredentialsStore credentialsStore;

    protected RepoClient(RepoClientConfig config) {
        this.config = config;
        this.credentialsStore = new CredentialsStore();
    }

    public abstract Set<GitRepo> getRemotes();

    public void processRepositories() {
        List<String> include = getConfig().getAll("include");
        List<String> exclude = getConfig().getAll("exclude");

        System.out.println();
        System.out.println("Connecting to GitHub " + getConfig().get("name") + " as " + getConfig().get("username"));

        Set<GitRepo> allRemotes = getRemotes();
        System.out.println(getConfig().get("name") + " - checking " + allRemotes.size() + " repositories");

        List<GitRepo> remotes = allRemotes.stream()
          .filter((repo) -> include.isEmpty() || include.contains(repo.getName()))
          .filter((repo) -> !exclude.contains(repo.getName()))
          .sorted()
          .collect(Collectors.toList());

        if (remotes.isEmpty() && !allRemotes.isEmpty()) {
            throw new RuntimeException(allRemotes.size() + " repos exist but all were filtered out!");
        }

        int[] counts = getCounts(remotes);
        System.out.println("cloning " + counts[0] + " branches in " + counts[1] + " repos and updating " + counts[2] + " branches in " + counts[3] + " repos");
        processRepos(remotes);
    }

    private int[] getCounts(List<GitRepo> remotes) {
        int[] result = new int[4];
        for (GitRepo repo : remotes) {
            Path localPath = repo.getLocalPath();
            Path config = localPath.resolve(".git").resolve("config");
            boolean exists = Files.exists(config);
            if (exists) {
                try (Git git = Git.open(localPath.toFile())) {
                    List<Ref> branches = git.branchList().call();
                    result[2] += branches.size();
                    result[3]++;
                } catch (Exception ignore) {
                    // Intentionally ignore exception
                    //System.err.println(exception.getMessage());
                }
            } else {
                try {
                    String remote = repo.getRemote();
                    Collection<Ref> heads = Git.lsRemoteRepository().setRemote(remote).setHeads(true).setCredentialsProvider(getGitCredentials(remote)).call();
                    result[0] += heads.size();
                    result[1]++;
                } catch (Exception ignore) {
                    // Intentionally ignore exception
                    //System.err.println(exception.getMessage());
                }
            }
        }
        return result;
    }

    private void processRepos(List<GitRepo> repos) {
        for (GitRepo repo : repos) {
            Path config = repo.getLocalPath().resolve(".git").resolve("config");
            if (Files.exists(config)) {
                pullRepo(repo);
            } else {
                cloneRepo(repo);
            }
        }
    }

    private void cloneRepo(GitRepo repo) {
        try {
            doGitClone(repo.getLocalPath(), repo.getRemote());
            printResult(repo, GitResult.CLONE_SUCCESS);
        } catch (Exception exception) {
            printResult(repo, exception);
        }
    }

    private void pullRepo(GitRepo repo) {
        Path localPath = repo.getLocalPath();
        try (Git git = Git.open(localPath.toFile())) {
            // Get the current branch
            String currentBranch = git.getRepository().getBranch();

            List<Ref> branches = git.branchList().call();
            for (Ref branch : branches) {
                try {
                    git.checkout().setName(branch.getName()).call();
                    int result = doGitPull(localPath);
                    printResult(repo, branch, result == 0 ? GitResult.PULL_UP_TO_DATE : GitResult.PULL_UPDATED);
                } catch (RefNotAdvertisedException exception) {
                    if (log.isDebugEnabled()) {
                        printResult(repo, branch, GitResult.MISSING);
                    }
                } catch (Exception exception) {
                    printResult(repo, branch, GitResult.ERROR, exception);
                }
            }

            // Go back to the current branch
            git.checkout().setName(currentBranch).call();
        } catch (RefNotFoundException exception) {
            if (log.isDebugEnabled()) {
                printResult(repo, GitResult.MISSING);
            }
        } catch (Exception exception) {
            printResult(repo, exception);
        }
    }

    private void printResult(GitRepo repo, GitResult result) {
        printResult(repo, null, result);
    }

    private void printResult(GitRepo repo, Ref branch, GitResult result) {
        printResult(repo, branch, result, null);
    }

    private void printResult(GitRepo repo, Exception exception) {
        printResult(repo, null, GitResult.ERROR, exception);
    }

    private void printResult(GitRepo repo, Ref branch, GitResult result, Exception exception) {
        String message = repo.getName() + ": " + repo.getLocalPath().toAbsolutePath();
        if (branch != null) {
            message += ":" + branch.getName();
        }
        if (exception != null) {
            message += ": " + exception.getClass().getSimpleName() + ": " + exception.getMessage();
        }

        PrintStream stream = result == GitResult.ERROR ? System.err : System.out;
        stream.println(result.getSymbol() + " " + message);
    }

    public int doGitPull(Path repo) throws IOException, GitAPIException, URISyntaxException {
        try (Git git = Git.open(repo.toFile())) {
            String uri = git.getRepository().getConfig().getString("remote", "origin", "url");

            PullResult result = git.pull().setCredentialsProvider(getGitCredentials(uri)).call();
            MergeResult.MergeStatus status = result.getMergeResult().getMergeStatus();
            return status == MergeResult.MergeStatus.ALREADY_UP_TO_DATE ? 0 : 1;
        }
    }

    public void doGitClone(Path repo, String uri) throws IOException, GitAPIException, URISyntaxException {
        URI base = new URI(uri);
        String username = getUsername(base);
        URI fullUri = new URI(base.getScheme(),
          URLEncoder.encode(username, StandardCharsets.UTF_8),
          base.getHost(),
          base.getPort(),
          base.getPath(),
          base.getQuery(),
          base.getFragment());

        Files.createDirectories(repo);
        try {
            Git.cloneRepository().setURI(fullUri.toASCIIString()).setDirectory(repo.toFile()).setCredentialsProvider(getGitCredentials(base)).call();
        } catch (Exception exception) {
            Files.delete(repo);
            throw exception;
        }
    }

    private CredentialsProvider getGitCredentials(String uri) throws URISyntaxException {
        return getGitCredentials(new URI(uri));
    }

    private CredentialsProvider getGitCredentials(URI uri) {
        return new UsernamePasswordCredentialsProvider(getUsername(uri), getPassword(uri));
    }

    protected RepoClientConfig getConfig() {
        return config;
    }

    protected CredentialsStore getCredentialsStore() {
        return credentialsStore;
    }

    protected RestTemplate getRest(URI uri) {
        RestTemplate rest = new RestTemplate();
        rest.getInterceptors().add(new BasicAuthenticationInterceptor(getUsername(uri), getPassword(uri)));
        return rest;
    }

    protected UriTemplate getUriTemplate(String path) {
        String endpoint = null;
        if (getConfig().exists("uri")) {
            endpoint = getConfig().get("uri");
        }
        if (endpoint == null) {
            endpoint = getConfig().get(getConfig().get("type") + "-default-uri");
        }
        return new UriTemplate(endpoint + path);
    }

    protected String replaceVariables(String source) {
        String result = source;
        File home = new File(System.getProperty("user.home"));
        result = result.replace("$HOME", home.toURI().getPath());
        return result;
    }

    private String getUsername(URI uri) {
        String username = null;
        if (username == null) {
            username = getConfig().get("username");
        }
        if (username == null) {
            username = Uris.getUsername(uri);
        }
        if (username == null) {
            username = getCredentialsStore().getUsername(uri.getHost());
        }
        return username;
    }

    private String getPassword(URI uri) {
        String password = null;
        if (password == null) {
            password = getConfig().get("password");
        }
        if (password == null) {
            password = Uris.getPassword(uri);
        }
        if (password == null) {
            password = getCredentialsStore().getPassword(uri.getHost());
        }
        return password;
    }
}

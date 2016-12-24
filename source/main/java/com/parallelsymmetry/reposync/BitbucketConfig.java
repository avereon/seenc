package com.parallelsymmetry.reposync;

import java.util.List;

public class BitbucketConfig {

	private String protocol;

	private String username;

	private String password;

	private String team;

	private List<String> projects;

	private String repoUri;

	private String target;

	public BitbucketConfig() {}

	public void setProjects( List<String> projects ) {
		this.projects = projects;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol( String protocol ) {
		this.protocol = protocol;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername( String username ) {
		this.username = username;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword( String password ) {
		this.password = password;
	}

	public String getTeam() {
		return team;
	}

	public void setTeam( String team ) {
		this.team = team;
	}

	public String getRepoUri() {
		return this.repoUri;
	}

	public void setRepoUri( String repoUri ) {
		this.repoUri = repoUri;
	}

	public String getTarget() {
		return this.target;
	}

	public void setTarget( String target ) {
		this.target = target;
	}

	public boolean equals( Object o ) {
		if( o == this ) return true;
		if( !(o instanceof BitbucketConfig) ) return false;
		final BitbucketConfig other = (BitbucketConfig)o;
		if( !other.canEqual( (Object)this ) ) return false;
		final Object this$username = this.getUsername();
		final Object other$username = other.getUsername();
		if( this$username == null ? other$username != null : !this$username.equals( other$username ) ) return false;
		final Object this$tmpltProjectRepos = this.getRepoUri();
		final Object other$tmpltProjectRepos = other.getRepoUri();
		if( this$tmpltProjectRepos == null ? other$tmpltProjectRepos != null : !this$tmpltProjectRepos.equals( other$tmpltProjectRepos ) ) return false;
		final Object this$tmpltProjectDir = this.getTarget();
		final Object other$tmpltProjectDir = other.getTarget();
		if( this$tmpltProjectDir == null ? other$tmpltProjectDir != null : !this$tmpltProjectDir.equals( other$tmpltProjectDir ) ) return false;
		final Object this$protocol = this.getProtocol();
		final Object other$protocol = other.getProtocol();
		if( this$protocol == null ? other$protocol != null : !this$protocol.equals( other$protocol ) ) return false;
		return true;
	}

	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $username = this.getUsername();
		result = result * PRIME + ($username == null ? 43 : $username.hashCode());
		final Object $tmpltProjectRepos = this.getRepoUri();
		result = result * PRIME + ($tmpltProjectRepos == null ? 43 : $tmpltProjectRepos.hashCode());
		final Object $tmpltProjectDir = this.getTarget();
		result = result * PRIME + ($tmpltProjectDir == null ? 43 : $tmpltProjectDir.hashCode());
		final Object $tmpltProtocol = this.getProtocol();
		result = result * PRIME + ($tmpltProtocol == null ? 43 : $tmpltProtocol.hashCode());
		return result;
	}

	protected boolean canEqual( Object other ) {
		return other instanceof BitbucketConfig;
	}

	public String toString() {
		return "BitbucketConfig(" + "protocol=" + this.getProtocol() + ", team=" + this.getTeam() + ", username=" + this.getUsername() + ", projectRepos=" + this.getRepoUri() + ", projectDir=" + this.getTarget() + ")";
	}
}

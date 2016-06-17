package com.github.moduth.domain.model.repos;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Abner on 16/6/17.
 * Email nimengbo@gmail.com
 * github https://github.com/nimengbo
 */
public class ReposEntity {



    /**
     * id : 50404923
     * name : awesome-data-structure
     * full_name : moduth/awesome-data-structure
     * owner : {"login":"moduth","id":16304115,"avatar_url":"https://avatars.githubusercontent.com/u/16304115?v=3","gravatar_id":"","url":"https://api.github.com/users/moduth","html_url":"https://github.com/moduth","followers_url":"https://api.github.com/users/moduth/followers","following_url":"https://api.github.com/users/moduth/following{/other_user}","gists_url":"https://api.github.com/users/moduth/gists{/gist_id}","starred_url":"https://api.github.com/users/moduth/starred{/owner}{/repo}","subscriptions_url":"https://api.github.com/users/moduth/subscriptions","organizations_url":"https://api.github.com/users/moduth/orgs","repos_url":"https://api.github.com/users/moduth/repos","events_url":"https://api.github.com/users/moduth/events{/privacy}","received_events_url":"https://api.github.com/users/moduth/received_events","type":"Organization","site_admin":false}
     * private : false
     * html_url : https://github.com/moduth/awesome-data-structure
     * description : Android app showing Java implementation of interesting data structures and algorithms.
     * fork : false
     * url : https://api.github.com/repos/moduth/awesome-data-structure
     * forks_url : https://api.github.com/repos/moduth/awesome-data-structure/forks
     * keys_url : https://api.github.com/repos/moduth/awesome-data-structure/keys{/key_id}
     * collaborators_url : https://api.github.com/repos/moduth/awesome-data-structure/collaborators{/collaborator}
     * teams_url : https://api.github.com/repos/moduth/awesome-data-structure/teams
     * hooks_url : https://api.github.com/repos/moduth/awesome-data-structure/hooks
     * issue_events_url : https://api.github.com/repos/moduth/awesome-data-structure/issues/events{/number}
     * events_url : https://api.github.com/repos/moduth/awesome-data-structure/events
     * assignees_url : https://api.github.com/repos/moduth/awesome-data-structure/assignees{/user}
     * branches_url : https://api.github.com/repos/moduth/awesome-data-structure/branches{/branch}
     * tags_url : https://api.github.com/repos/moduth/awesome-data-structure/tags
     * blobs_url : https://api.github.com/repos/moduth/awesome-data-structure/git/blobs{/sha}
     * git_tags_url : https://api.github.com/repos/moduth/awesome-data-structure/git/tags{/sha}
     * git_refs_url : https://api.github.com/repos/moduth/awesome-data-structure/git/refs{/sha}
     * trees_url : https://api.github.com/repos/moduth/awesome-data-structure/git/trees{/sha}
     * statuses_url : https://api.github.com/repos/moduth/awesome-data-structure/statuses/{sha}
     * languages_url : https://api.github.com/repos/moduth/awesome-data-structure/languages
     * stargazers_url : https://api.github.com/repos/moduth/awesome-data-structure/stargazers
     * contributors_url : https://api.github.com/repos/moduth/awesome-data-structure/contributors
     * subscribers_url : https://api.github.com/repos/moduth/awesome-data-structure/subscribers
     * subscription_url : https://api.github.com/repos/moduth/awesome-data-structure/subscription
     * commits_url : https://api.github.com/repos/moduth/awesome-data-structure/commits{/sha}
     * git_commits_url : https://api.github.com/repos/moduth/awesome-data-structure/git/commits{/sha}
     * comments_url : https://api.github.com/repos/moduth/awesome-data-structure/comments{/number}
     * issue_comment_url : https://api.github.com/repos/moduth/awesome-data-structure/issues/comments{/number}
     * contents_url : https://api.github.com/repos/moduth/awesome-data-structure/contents/{+path}
     * compare_url : https://api.github.com/repos/moduth/awesome-data-structure/compare/{base}...{head}
     * merges_url : https://api.github.com/repos/moduth/awesome-data-structure/merges
     * archive_url : https://api.github.com/repos/moduth/awesome-data-structure/{archive_format}{/ref}
     * downloads_url : https://api.github.com/repos/moduth/awesome-data-structure/downloads
     * issues_url : https://api.github.com/repos/moduth/awesome-data-structure/issues{/number}
     * pulls_url : https://api.github.com/repos/moduth/awesome-data-structure/pulls{/number}
     * milestones_url : https://api.github.com/repos/moduth/awesome-data-structure/milestones{/number}
     * notifications_url : https://api.github.com/repos/moduth/awesome-data-structure/notifications{?since,all,participating}
     * labels_url : https://api.github.com/repos/moduth/awesome-data-structure/labels{/name}
     * releases_url : https://api.github.com/repos/moduth/awesome-data-structure/releases{/id}
     * deployments_url : https://api.github.com/repos/moduth/awesome-data-structure/deployments
     * created_at : 2016-01-26T05:10:20Z
     * updated_at : 2016-01-26T05:10:20Z
     * pushed_at : 2016-01-26T05:10:21Z
     * git_url : git://github.com/moduth/awesome-data-structure.git
     * ssh_url : git@github.com:moduth/awesome-data-structure.git
     * clone_url : https://github.com/moduth/awesome-data-structure.git
     * svn_url : https://github.com/moduth/awesome-data-structure
     * homepage : null
     * size : 4
     * stargazers_count : 0
     * watchers_count : 0
     * language : null
     * has_issues : true
     * has_downloads : true
     * has_wiki : true
     * has_pages : false
     * forks_count : 0
     * mirror_url : null
     * open_issues_count : 0
     * forks : 0
     * open_issues : 0
     * watchers : 0
     * default_branch : master
     */

    @SerializedName("id")
    public int id;
    @SerializedName("name")
    public String name;
    @SerializedName("full_name")
    public String fullName;
    /**
     * login : moduth
     * id : 16304115
     * avatar_url : https://avatars.githubusercontent.com/u/16304115?v=3
     * gravatar_id :
     * url : https://api.github.com/users/moduth
     * html_url : https://github.com/moduth
     * followers_url : https://api.github.com/users/moduth/followers
     * following_url : https://api.github.com/users/moduth/following{/other_user}
     * gists_url : https://api.github.com/users/moduth/gists{/gist_id}
     * starred_url : https://api.github.com/users/moduth/starred{/owner}{/repo}
     * subscriptions_url : https://api.github.com/users/moduth/subscriptions
     * organizations_url : https://api.github.com/users/moduth/orgs
     * repos_url : https://api.github.com/users/moduth/repos
     * events_url : https://api.github.com/users/moduth/events{/privacy}
     * received_events_url : https://api.github.com/users/moduth/received_events
     * type : Organization
     * site_admin : false
     */

    @SerializedName("owner")
    public OwnerBean owner;
    @SerializedName("private")
    public boolean privateX;
    @SerializedName("html_url")
    public String htmlUrl;
    @SerializedName("description")
    public String description;
    @SerializedName("fork")
    public boolean fork;
    @SerializedName("url")
    public String url;
    @SerializedName("forks_url")
    public String forksUrl;
    @SerializedName("keys_url")
    public String keysUrl;
    @SerializedName("collaborators_url")
    public String collaboratorsUrl;
    @SerializedName("teams_url")
    public String teamsUrl;
    @SerializedName("hooks_url")
    public String hooksUrl;
    @SerializedName("issue_events_url")
    public String issueEventsUrl;
    @SerializedName("events_url")
    public String eventsUrl;
    @SerializedName("assignees_url")
    public String assigneesUrl;
    @SerializedName("branches_url")
    public String branchesUrl;
    @SerializedName("tags_url")
    public String tagsUrl;
    @SerializedName("blobs_url")
    public String blobsUrl;
    @SerializedName("git_tags_url")
    public String gitTagsUrl;
    @SerializedName("git_refs_url")
    public String gitRefsUrl;
    @SerializedName("trees_url")
    public String treesUrl;
    @SerializedName("statuses_url")
    public String statusesUrl;
    @SerializedName("languages_url")
    public String languagesUrl;
    @SerializedName("stargazers_url")
    public String stargazersUrl;
    @SerializedName("contributors_url")
    public String contributorsUrl;
    @SerializedName("subscribers_url")
    public String subscribersUrl;
    @SerializedName("subscription_url")
    public String subscriptionUrl;
    @SerializedName("commits_url")
    public String commitsUrl;
    @SerializedName("git_commits_url")
    public String gitCommitsUrl;
    @SerializedName("comments_url")
    public String commentsUrl;
    @SerializedName("issue_comment_url")
    public String issueCommentUrl;
    @SerializedName("contents_url")
    public String contentsUrl;
    @SerializedName("compare_url")
    public String compareUrl;
    @SerializedName("merges_url")
    public String mergesUrl;
    @SerializedName("archive_url")
    public String archiveUrl;
    @SerializedName("downloads_url")
    public String downloadsUrl;
    @SerializedName("issues_url")
    public String issuesUrl;
    @SerializedName("pulls_url")
    public String pullsUrl;
    @SerializedName("milestones_url")
    public String milestonesUrl;
    @SerializedName("notifications_url")
    public String notificationsUrl;
    @SerializedName("labels_url")
    public String labelsUrl;
    @SerializedName("releases_url")
    public String releasesUrl;
    @SerializedName("deployments_url")
    public String deploymentsUrl;
    @SerializedName("created_at")
    public String createdAt;
    @SerializedName("updated_at")
    public String updatedAt;
    @SerializedName("pushed_at")
    public String pushedAt;
    @SerializedName("git_url")
    public String gitUrl;
    @SerializedName("ssh_url")
    public String sshUrl;
    @SerializedName("clone_url")
    public String cloneUrl;
    @SerializedName("svn_url")
    public String svnUrl;
    @SerializedName("homepage")
    public Object homepage;
    @SerializedName("size")
    public int size;
    @SerializedName("stargazers_count")
    public int stargazersCount;
    @SerializedName("watchers_count")
    public int watchersCount;
    @SerializedName("language")
    public Object language;
    @SerializedName("has_issues")
    public boolean hasIssues;
    @SerializedName("has_downloads")
    public boolean hasDownloads;
    @SerializedName("has_wiki")
    public boolean hasWiki;
    @SerializedName("has_pages")
    public boolean hasPages;
    @SerializedName("forks_count")
    public int forksCount;
    @SerializedName("mirror_url")
    public Object mirrorUrl;
    @SerializedName("open_issues_count")
    public int openIssuesCount;
    @SerializedName("forks")
    public int forks;
    @SerializedName("open_issues")
    public int openIssues;
    @SerializedName("watchers")
    public int watchers;
    @SerializedName("default_branch")
    public String defaultBranch;

    public static class OwnerBean {
        @SerializedName("login")
        public String login;
        @SerializedName("id")
        public int id;
        @SerializedName("avatar_url")
        public String avatarUrl;
        @SerializedName("gravatar_id")
        public String gravatarId;
        @SerializedName("url")
        public String url;
        @SerializedName("html_url")
        public String htmlUrl;
        @SerializedName("followers_url")
        public String followersUrl;
        @SerializedName("following_url")
        public String followingUrl;
        @SerializedName("gists_url")
        public String gistsUrl;
        @SerializedName("starred_url")
        public String starredUrl;
        @SerializedName("subscriptions_url")
        public String subscriptionsUrl;
        @SerializedName("organizations_url")
        public String organizationsUrl;
        @SerializedName("repos_url")
        public String reposUrl;
        @SerializedName("events_url")
        public String eventsUrl;
        @SerializedName("received_events_url")
        public String receivedEventsUrl;
        @SerializedName("type")
        public String type;
        @SerializedName("site_admin")
        public boolean siteAdmin;
    }
}

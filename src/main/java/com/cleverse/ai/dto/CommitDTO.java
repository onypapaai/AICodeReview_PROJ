package com.cleverse.ai.dto;

public class CommitDTO {

	
	private String commitId="";
	private String comment="";
	private String commiter="";
	private String commiterName="";
	private String branchId="";
	private String branchName="";
	private String projectName="";
	private Long projectNo=0L;
	
	
	
	public String getCommitId() {
		return commitId;
	}
	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getCommiter() {
		return commiter;
	}
	public void setCommiter(String commiter) {
		this.commiter = commiter;
	}
	public String getCommiterName() {
		return commiterName;
	}
	public void setCommiterName(String commiterName) {
		this.commiterName = commiterName;
	}
	public String getBranchId() {
		return branchId;
	}
	public void setBranchId(String branchId) {
		this.branchId = branchId;
	}
	public String getBranchName() {
		return branchName;
	}
	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public Long getProjectNo() {
		return projectNo;
	}
	public void setProjectNo(Long projectNo) {
		this.projectNo = projectNo;
	}
	
	
	
}

package com.cleverse.ai.dto;

public class ProgramDTO {

	
	private String projectName="";
	private String projectUrl="";
	
	private Long projectNo=0L;
	
	
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
	public String getProjectUrl() {
		return projectUrl;
	}
	public void setProjectUrl(String projectUrl) {
		this.projectUrl = projectUrl;
	}
}

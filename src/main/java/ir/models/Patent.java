package ir.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;
/**
 * 专利实体映射类
 * @author 杨涛
 *
 */
@Entity
@Table(name="t_patents",uniqueConstraints= {@UniqueConstraint(columnNames= {"id"})})
public class Patent {

	/**
	 * 专利条目编号
	 */
	@Id
	@GenericGenerator(name="generator",strategy="assigned")
	@GeneratedValue(generator="generator")
	@Column(name="id")
	private String id;
	
	/**
	 * 专利摘要
	 */
	@Column(name="abstract")
	private String patent_Abstract;
	
	/**
	 * 专利地址
	 */
	@Column(name="addreass")
	private String address;
	
	/**
	 * 专利申请单位、个人
	 */
	@Column(name="applicant")
	private String applicant;
	
	
	/**
	 * 公开日期
	 */
	@Column(name="application_date")
	private String application_date;
	
	/**
	 * 申请号码
	 */
	@Column(name="application_number")
	private String application_number;
	
	/**
	 * 公开号码
	 */
	@Column(name="application_publish_number")
	private String application_publish_number;
	
	/**
	 * 分类号
	 */
	@Column(name="classification_number")
	private String classification_number;
	
	/**
	 * 申请日期
	 */
	@Column(name="filling_date")
	private String filling_date;
	
	/**
	 * 授权状态
	 */
	@Column(name="grant_status")
	private String grant_status;
	
	/**
	 * 发明者
	 */
	@Column(name="inventor")
	private String inventor;

	/**
	 * 标题
	 */
	@Column(name="title")
	private String title;
	
	/**
	 * 年份
	 */
	@Column(name="year")
	private int year;
	

	@Override
	public String toString() {
		return "Patent [id=" + id + ", patent_Abstract=" + patent_Abstract + ", address=" + address + ", applicant="
				+ applicant + ", application_date=" + application_date + ", application_number=" + application_number
				+ ", application_publish_number=" + application_publish_number + ", classification_number="
				+ classification_number + ", filling_date=" + filling_date + ", grant_status=" + grant_status
				+ ", inventor=" + inventor + ", title=" + title + ", year=" + year +"]";
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getPatent_Abstract() {
		return patent_Abstract;
	}


	public void setPatent_Abstract(String patent_Abstract) {
		this.patent_Abstract = patent_Abstract;
	}


	public String getAddress() {
		return address;
	}


	public void setAddress(String address) {
		this.address = address;
	}


	public String getApplicant() {
		return applicant;
	}


	public void setApplicant(String applicant) {
		this.applicant = applicant;
	}


	public String getApplication_date() {
		return application_date;
	}


	public void setApplication_date(String application_date) {
		this.application_date = application_date;
	}


	public String getApplication_number() {
		return application_number;
	}


	public void setApplication_number(String application_number) {
		this.application_number = application_number;
	}


	public String getApplication_publish_number() {
		return application_publish_number;
	}


	public void setApplication_publish_number(String application_publish_number) {
		this.application_publish_number = application_publish_number;
	}


	public String getClassification_number() {
		return classification_number;
	}


	public void setClassification_number(String classification_number) {
		this.classification_number = classification_number;
	}


	public String getFilling_date() {
		return filling_date;
	}


	public void setFilling_date(String filling_date) {
		this.filling_date = filling_date;
	}


	public String getGrant_status() {
		return grant_status;
	}


	public void setGrant_status(String grant_status) {
		this.grant_status = grant_status;
	}


	public String getInventor() {
		return inventor;
	}


	public void setInventor(String inventor) {
		this.inventor = inventor;
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public int getYear() {
		return year;
	}


	public void setYear(int year) {
		this.year = year;
	}

	

	
}

package ir.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;

import lombok.Data;
/**
 * 专利实体映射类
 * @author 杨涛
 *
 */
@Data
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
	private int grant_status;
	
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

	

	
}

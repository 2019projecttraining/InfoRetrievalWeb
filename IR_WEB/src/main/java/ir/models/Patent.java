package ir.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;

import lombok.Data;

@Data
@Entity
@Table(name="t_patents",uniqueConstraints= {@UniqueConstraint(columnNames= {"id"})})
public class Patent {

	@Id
	@GenericGenerator(name="generator",strategy="assigned")
	@GeneratedValue(generator="generator")
	@Column(name="id")
	private String id;
	
	@Column(name="abstract")
	private String patent_Abstract;
	
	@Column(name="addreass")
	private String address;
	
	@Column(name="applicant")
	private String applicant;
	
	@Column(name="application_date")
	private String application_date;
	
	@Column(name="application_number")
	private String application_number;
	
	@Column(name="application_publish_number")
	private String application_publish_number;
	
	@Column(name="classification_number")
	private String classification_number;
	
	@Column(name="filling_date")
	private String filling_date;
	
	@Column(name="grant_status")
	private int grant_status;
	
	@Column(name="inventor")
	private String inventor;

	
	@Column(name="title")
	private String title;
	
	@Column(name="year")
	private int year;
	
	@Column(name="grant_odd")
	private double grant_odd;

	@Override
	public String toString() {
		return "Patent [id=" + id + ", patent_Abstract=" + patent_Abstract + ", address=" + address + ", applicant="
				+ applicant + ", application_date=" + application_date + ", application_number=" + application_number
				+ ", application_publish_number=" + application_publish_number + ", classification_number="
				+ classification_number + ", filling_date=" + filling_date + ", grant_status=" + grant_status
				+ ", inventor=" + inventor + ", title=" + title + ", year=" + year + ", grant_odd=" + grant_odd + "]";
	}

	

	
}

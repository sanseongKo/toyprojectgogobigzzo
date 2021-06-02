package com.test.board.domain;

import java.sql.Date;

public class MemberVO {
	private int uid;
	private String name;
	private String password;
	private String email;
	private String phone;
	private int verification;
	private String nickName;
	private Date regdate;
	
	public MemberVO() {}

	public MemberVO(int uid, String name, String password, String email, String phone, int verification,
			String nickName, Date regdate) {
		super();
		this.uid = uid;
		this.name = name;
		this.password = password;
		this.email = email;
		this.phone = phone;
		this.verification = verification;
		this.nickName = nickName;
		this.regdate = regdate;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public int getVerification() {
		return verification;
	}

	public void setVerification(int verification) {
		this.verification = verification;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public Date getRegdate() {
		return regdate;
	}

	public void setRegdate(Date regdate) {
		this.regdate = regdate;
	}
	
	
	
}
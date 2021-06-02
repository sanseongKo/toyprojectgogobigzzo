<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
<form action ="<c:url value = "naverRegister"/>" method="POST">
	<c:choose>
		<c:when test="${sessionId != null}">
				<div>이메일</div>
				<div>
					<input name="email" value = "${email}" disabled="disabled">
				</div>
				<div>이름</div>
				<div>
					<input name = "name" value = "${name}" disabled="disabled">
				</div>
		</c:when>
	</c:choose>
			<div>
			<div>핸드폰 번호</div>
			<div><input name="phone" required></div>
		</div>
		<div>
			<div>비밀번호</div>
			<div><input name="password" required></div>
		</div>
		<div>
			<div>별명</div>
			<div><input name="nickname" required></div>
		</div>
		
		<div class="check_register">
			<input type="submit" value="회원가입">
			
		</div>
		<div>
			<a href="<c:url value="naverLogin"/>">취소</a>
		</div>
		
		
	</form>
</body>
</html>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>${contentVO.title}&nbsp내용</title>
</head>
<body>

		<div>
			<table border="1">
				<c:choose>
					<c:when test="${contentVO.on_off == 1}">
					<form action="/board/payStep1" method="post" onSubmit="goStep1(lastAmount)">
						<tr>
							<th>온라인 클래스</th>
						</tr>
						
						<tr>
							<th>상품개수</th>
							<td><select id="quantity" name="quantity" onchange="changeSelect(code)">
									<option value="" selected disabled>주문개수</option>
    								<option value="1">1</option>
    								<option value="2">2</option>
    								<option value="3">3</option>
    								<option value="4">4</option>
								</select><br></td>
						</tr>
						<tr>
							<th>결제금액 </th>
							<td><div id="pay" class="pay"></div></td>
						</tr>
						<tr>
							<th>지역</th>
							<td>${contentVO.area}</td>
						</tr>
						<input type="submit" value="결제하기"/>
						<input type="hidden" id="amount" name="amount" value="" />
						<input type="hidden" id="cid" name="cid" value="${contentVO.cid}"/>
						<input type="hidden" id="uid" name="uid" value="${memberVO.uid}"/>
						<input type="hidden" id="title" name="title" value="${contentVO.title}"/>
	
						</form>
					</c:when>

					<c:when test="${contentVO.on_off == 2}">
						
						
						
<form action="/board/payStep1" method="post" onSubmit="goStep1(lastAmount)">
<!-- <form action="${pageContext.request.contextPath}/reserveComplete" method="post"> -->
<div class="reservation">
  	<p>예약일</p>
  	<input id="resday" name="resday" type="text" class="resday" placeholder="예약일 확인" required/>
  	<br>
  	예약가능 인원 : <h2><div class="getPersonNumber"></div></h2>
  	
  	
  	<br>예약선택  
  	
	<select id="quantity" name="quantity" onchange="changeSelect(code)">
		<option value="" selected disabled>인원 선택</option>
    	<option value="1">1인</option>
    	<option value="2">2인</option>
    	<option value="3">3인</option>
    	<option value="4">4인</option>
	</select><br><br>

	결제금액  : <div id="pay" class="pay"></div>
	<br><br>
	<input type="submit" value="결제하기"/>
	
	<input type="hidden" id="amount" name="amount" value="" />
	<input type="hidden" id="cid" name="cid" value="${contentVO.cid}"/>
	<input type="hidden" id="uid" name="uid" value="${memberVO.uid}"/>
	<input type="hidden" id="title" name="title" value="${contentVO.title}"/>


</div>
</form>
				
					</c:when>

					<c:otherwise>
						<tr>
							<th>무슨 클래스게~?</th>
						</tr>
					</c:otherwise>
				</c:choose>
			</table>

		</div>
		
		<h3>클래스 내용</h3>
		
		<div>
			<table  border="1">
				<tr>
					<th>제목</th>
					<td>${contentVO.title}</td>
				</tr>

				<c:if test="${contentVO.file_name ne null}">
					<tr>
						<th>첨부파일</th>
						<td><a href="/board/down/${contentVO.file_name}?fileName=${contentVO.file_name}">${contentVO.file_name}</a></td>
					</tr>
				</c:if>

				<tr>
					<th>내용</th>
					<td><textarea name="content" cols="40" rows="10">${contentVO.content}</textarea></td>
				</tr>

				<tr>
					<th>판매자</th>
					<td>${contentVO.uid}</td>
				</tr>
				<tr>
					<th>작성일</th>
					<td>${contentVO.condate}</td>
				</tr>
				
			</table>
		</div>
		<div>
			<img src="/board/imgRead/${contentVO.pic_content}?fileName=${contentVO.pic_content}" style="width:300px; height:100px"/>
			<img src="${contentVO.picFile}" style="width:300px; height:100px"/>
		</div>
		<div>
			<c:choose>
					<c:when test="${contentVO.on_off == 1}">
						<%@ include file="/WEB-INF/views/board/onRead.jsp" %>
					</c:when>

					<c:when test="${contentVO.on_off == 2}">
						<%@ include file="/WEB-INF/views/board/offRead.jsp" %>
					</c:when>

					<c:otherwise>
						<tr>
							<th>무슨 클래스게~?</th>
						</tr>
					</c:otherwise>
				</c:choose>
		</div>

	<div>
		<c:choose>
			<c:when test="${memberVO.verification == 3}">
				<!-- 관리자일 경우	:	글 수정/삭제 버튼 + 목록 버튼 -->
				<a href="<c:url value="/edit/${contentVO.cid}"/>">수정</a> 
				<a href="<c:url value="/delete/${contentVO.cid}"/>">삭제</a> 
				<a href="<c:url value="/"/>">목록</a>
			</c:when>
			<c:otherwise>
				<!-- 판매자 및 구매자일 경우		:	 목록 버튼만 -->
				<button onClick="location.href='<c:url value="/edit/${contentVO.cid}"/>'">수정</button>
				<button onClick="location.href='<c:url value="/delete/${contentVO.cid}"/>'">삭제</button>
				<button onClick="history.go(-1)">이전 목록</button>
			</c:otherwise>
		</c:choose>
	</div>
	
	<div>
		<form:form commandName="replyVO" method="POST">
			<%@ include file="/WEB-INF/views/reply/repWrite.jsp" %>
		</form:form>
	</div>
	
	<div>
		<%@ include file="/WEB-INF/views/reply/repList.jsp" %>
	</div>




<script>
//submit 시에 hidden 으로 총 금액 amount 에 value가 들어감 

//code는 ajax로 받아온 인원수
var code = "";

function goStep1(lastAmount) {
	var lastAmountt = $(".pay").val();
	document.getElementById('amount').value=lastAmount;

}

//4)  선택인원에 따라 결제금액 바꿔줌
var pay = document.querySelector('#pay');
var price= ${contentVO.price};
var lastAmount;

var over='인원수초과';
function changeSelect(code) {
	var langSelect = document.getElementById("quantity");
	var selectValue = langSelect.options[langSelect.selectedIndex].value;
	
	
	pay.innerHTML = selectValue*price;
	lastAmount = selectValue*price;
	console.log(lastAmount);
	if (selectValue > code) {
		alert('인원수가 초과되었습니다. 다시 선택해주세욥');
		pay.innerHTML = '0';
	}
	
}

//1) 달력에 날짜표시
//eable: ["2020-02-18", "2020-02-19", "2020-02-21", "2020-02-20", "2020-02-27", "2020-02-26"] 
var resday = document.querySelector('.resday');
resday.flatpickr(
		{			
			inline: true,
			dateFormat: "Y-m-d",
			enable :  ${dateList}
		} 
);

//2) 클릭 날짜에 따른 이벤트  
//db안에 날짜 받아서 달력에 표시
var clickPart = document.getElementById("resday");
var showNumber=clickPart.addEventListener('change',onClick);


//3)날짜별 예약인원 표시
//key=날짜, value=인원수 Ajax로 value 를 받아와서 띄어줌 

function onClick() {
	var inputcode = $(".resday").val();
	//cid는 content.jsp에서 vo객체로 받아오는 값을 넣어주기
	var num = $(".getPersonNumber");
	if (inputcode !=null) {
		var personNum = "${dateMap.inputcode}";
		//document.getElementById('getPersonNumber').innerHTML=personNum;
		$.ajax({
			type:"GET",
			url:"getPersonNumber?inputcode="+inputcode+"&cid="+ ${contentVO.cid},
			success:function(data){
				code = data;
				num.html(code);
				console.log(code);
			}
		});
	}
}	




</script>

</body>
</html>
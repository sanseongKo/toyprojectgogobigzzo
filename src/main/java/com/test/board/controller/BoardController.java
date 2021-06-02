package com.test.board.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.test.board.dao.RegisterDao;
import com.test.board.domain.ContentVO;
import com.test.board.domain.MemberVO;
import com.test.board.domain.ReplyVO;
import com.test.board.domain.ResDays;
import com.test.board.login.KakaoService;
import com.test.board.login.NaverLoginBO;
import com.test.board.service.BoardService;
import com.test.board.service.ContentService;

@Controller
@RequestMapping
public class BoardController {
	
	@Autowired
	private RegisterDao registerDao;
	@Autowired
	private KakaoService kakaoService;	

	@Autowired
	private NaverLoginBO naverLoginBO;
	private String apiResult = null;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private ContentService contentService;
	
	@Autowired
	private BoardService boardService;
	
	/*@Resource(name="uploadPath")
	private String uploadPath;*/
	
	//濡쒓렇�씤 �럹�씠吏� �씠�룞
	@RequestMapping(value="/main")
	public String main() {
		return "login/naverLogin";
	}
	
	//濡쒓렇�씤 泥� �솕硫� �슂泥� 硫붿냼�뱶
	@RequestMapping(value = "/login", method = { RequestMethod.GET, RequestMethod.POST })
	public String login(Model model, HttpSession session) {
		System.out.println(session);
		String naverAuthUrl = naverLoginBO.getAuthorizationUrl(session);

		//https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=sE***************&
		//redirect_uri=http%3A%2F%2F211.63.89.90%3A8090%2Flogin_project%2Fcallback&state=e68c269c-5ba9-4c31-85da-54c16c658125
		//�꽕�씠踰�
		model.addAttribute("url", naverAuthUrl);

		return "login/main";
	}
	
	//濡쒓렇�씤 �솕硫�
	@RequestMapping(value="/register", method = RequestMethod.POST)
	public String registerPost(@ModelAttribute MemberVO memberVO, HttpSession session) {
		
		registerDao.register(memberVO);
		
		return "login/main";
	}

	//�꽕�씠踰� 濡쒓렇�씤 �꽦怨듭떆 callback�샇異� 硫붿냼�뱶
	@RequestMapping(value = "/callback", method = { RequestMethod.GET, RequestMethod.POST })
	public String callback(Model model, @RequestParam String code, @RequestParam String state, HttpSession session) throws IOException, ParseException {

		OAuth2AccessToken oauthToken;
		oauthToken = naverLoginBO.getAccessToken(session, code, state);
		//1. 濡쒓렇�씤 �궗�슜�옄 �젙蹂대�� �씫�뼱�삩�떎.

		apiResult = naverLoginBO.getUserProfile(oauthToken); //String�삎�떇�쓽 json�뜲�씠�꽣
		/** apiResult json 援ъ“
	   {"resultcode":"00",
	   "message":"success",
	   "response":{"id":"33666449","nickname":"shinn****","age":"20-29","gender":"M","email":"sh@naver.com","name":"\uc2e0\ubc94\ud638"}}
		 **/
		//2. String�삎�떇�씤 apiResult瑜� json�삎�깭濡� 諛붽퓞
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(apiResult);
		JSONObject jsonObj = (JSONObject) obj;
		//3. �뜲�씠�꽣 �뙆�떛
		//Top�젅踰� �떒怨� _response �뙆�떛
		JSONObject response_obj = (JSONObject)jsonObj.get("response");

		//response�쓽 nickname媛� �뙆�떛
		String nickname = (String)response_obj.get("nickname");
		String email = (String)response_obj.get("email");
		String name = (String)response_obj.get("name");
		System.out.println(nickname+ "," + email + "," + name);
		//4.�뙆�떛 �땳�꽕�엫 �꽭�뀡�쑝濡� ���옣
		session.setAttribute("sessionId",nickname); //�꽭�뀡 �깮�꽦
		model.addAttribute("result", apiResult);
		session.setAttribute("email", email);
		session.setAttribute("name", name);
		return "login/naverReg";
	}

	//移댁뭅�삤 濡쒓렇�씤
	@RequestMapping(value = "/kakaoLogin")
	public String kakaoRegisgter(@RequestParam(value = "code", required = false) String code, Model model, HttpSession session) throws Exception{

		String access_Token = kakaoService.getAccessToken(code);
		HashMap<String, Object> userInfo = kakaoService.getUserInfo(access_Token);
		String email = (String) userInfo.get("email");
		String nickname = (String) userInfo.get("nickname");
		session.setAttribute("sessionId", nickname);
		session.setAttribute("email", email);
		return "login/kakaoReg";
	}	

	//濡쒓렇�븘�썐
	@RequestMapping(value = "/logout", method = { RequestMethod.GET, RequestMethod.POST })
	public String logout(HttpSession session)throws IOException {
		System.out.println("�뿬湲곕뒗 logout");
		session.invalidate();
		return "redirect:login";
	}
	
	//�쉶�썝媛��엯 �씠�룞
	@RequestMapping(value="/register", method = RequestMethod.GET)   
	public String registerGet() {


		return "login/register";
	}



	//�씠硫붿씪 �씤利�(ajax 鍮꾨룞湲� �슂泥� 遺�遺�) 
	@RequestMapping(value="/mailCheck", method=RequestMethod.GET)
	@ResponseBody
	public String mailCheckGET(String email) throws Exception{

		Random random = new Random();
		int checkNum = random.nextInt(88888)+11111;

		String title = "회원 가입 인증번호 입니다.";
		String content = "홈페이지를 방문해주셔서 감사합니다." +
				"<br><br>" + 
				"인증 번호는  " + checkNum + "입니다." + 
				"<br>" + 
				"해당 인증번호를 인증번호 확인란에 기입하여 주세요.";
		String setFrom = "tkstjd565@naver.com";
		String toMail = email;


		try {

			MimeMessage message = mailSender.createMimeMessage();
			System.out.println("message: "+message);
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
			helper.setFrom(setFrom);
			helper.setTo(toMail);
			helper.setSubject(title);
			helper.setText(content,true);
			mailSender.send(message);

		}catch(Exception e) {
			e.printStackTrace();
		}    
		String num= Integer.toString(checkNum);

		return num;      

	}
	

	//�씠硫붿씪 以묐났 �솗�씤
	@RequestMapping(value="/checkOverlab", method=RequestMethod.GET)
	@ResponseBody
	public boolean checkOverlab(String email) {
		boolean check = true;
		String emailForCheck = email;
		/*MemberVO vo= memberDao.selectList(email);
	      if(!vo.emapty()){
	      return check = false;
	         }*/
		return check;
	}


	// 硫붿씤�럹�씠吏�  
	//  /board/�슂泥�
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Model model) throws Exception{
		List<ContentVO> list = contentService.mainList();
		model.addAttribute("list",list);
		System.out.println(list);
		
		return "list/main";
	}
	
	
	
	
	// on_off 由ъ뒪�듃
	@RequestMapping("/onofflist")
	public String onofflist(Model model, @RequestParam int on_off) throws Exception{
		List<ContentVO> list = contentService.mainList();
		model.addAttribute("list",list);

		List<ContentVO> onofflist = contentService.onoffList(on_off);
		model.addAttribute("onofflist",onofflist);

		return "list/onofflist";
	}
	
	
	// ��遺꾨쪟 由ъ뒪�듃
	@RequestMapping("/bigcatelist")
	public String bigcatelist(Model model, String big_name, int on_off) throws Exception{
		List<ContentVO> list = contentService.mainList();
		model.addAttribute("list",list);

		System.out.println(list);

		List<ContentVO> bigcatelist = contentService.bigcateList(big_name, on_off);
		model.addAttribute("bigcatelist",bigcatelist);
		System.out.println(bigcatelist);

		return "list/bigcatelist";
	}
	
	// �냼遺꾨쪟 由ъ뒪�듃
	@RequestMapping("/smallcatelist")
	public String smallcatelist(Model model, String small_name, int on_off) throws Exception{
		List<ContentVO> list = contentService.mainList();
		model.addAttribute("list",list);
		System.out.println(list);

		List<ContentVO> smallcatelist = contentService.smallcateList(small_name, on_off);
		model.addAttribute("smallcatelist",smallcatelist);
		System.out.println(smallcatelist);

		return "list/smallcatelist";
	}
	
	// �떊洹� 由ъ뒪�듃
	@RequestMapping("/newlist")
	public String newlist(Model model, int on_off) throws Exception{
		List<ContentVO> newlist = contentService.newList(on_off);
		model.addAttribute("newlist",newlist);


		return "list/newlist";
	}
	
	// 吏��뿭 由ъ뒪�듃
	@RequestMapping("/arealist")
	public String arealist(Model model, String area, int on_off) throws Exception{
		List<ContentVO> arealist = contentService.areaList(area, on_off);
		model.addAttribute("arealist",arealist);

		System.out.println(arealist);

		return "list/arealist";
	}



	// 寃뚯떆臾� �겢由��떆 �긽�꽭 �럹�씠吏� 
	// 湲� �씫湲� + �뙎湲� �옉�꽦 諛� �씫湲�
	@RequestMapping(value = "/contentRead/{cid}", method = RequestMethod.GET) // �럹�씠吏� 留곹겕 媛� c:url value="/board/read/${board.seq}" 湲� �씫湲�
	public String read(Model model, @PathVariable int cid) {
		ContentVO contentVO = contentService.select(cid);
		
		model.addAttribute("contentVO", contentVO);
		
		model.addAttribute("repList", contentService.repList(cid));
		model.addAttribute("replyVO", new ReplyVO());
		
		
		
		//�꽭�뀡 �븞�뿉 �궗�슜�옄 MEMBER �젙蹂� 諛쏆븘�삤湲�
				//session.se
				//媛��긽�쓽 cid 
				
				//model.addAttribute("cid", cid);
				//�궇吏� list濡� 戮묒븘�샂 
				//[{RESDAY=2021-05-02 20:19:09.0}, {RESDAY=2020-04-25 00:00:00.0}] �씠�윴 �삎�깭濡�  

		//�삤�봽�씪�씤 �씪�븣 
				if (contentVO.getOn_off()==2) {
					//�슦�꽑 cid濡� 
					List<String> dayList = boardService.getDays(cid);
					//1) List -> String 
					String date1 = dayList.toString();
					String[] date2 = date1.split(",");
					//2) String -> 諛곗뿴
					for (String list : date2) {
						System.out.println("list : "+ list);
					}

					//3) 諛곗뿴�븞 �뜲�씠�꽣 �궇吏쒗삎�깭濡� 媛�怨킺ate3: "2021-05-02","2020-04-25","2020-05-09","2020-05-16","2021-05-16",
					String date3 = "";
					for(int i = 0; i < date2.length; i++) {
						date3 += '\"' + date2[i].substring(9, 19) + '\"' + ",";
					}
					System.out.println("date3: "+date3);
				
					//4) �겙 �뵲�샂�몴 遺숈씤 �궇吏�(諛곗뿴) : ["2021-05-02", "2020-04-25", "2020-05-09", "2020-05-16", "2021-05-16"]
					String[] date4 = date3.split(",");
				
					System.out.println("�겙 �뵲�샂�몴 遺숈씤 �궇吏�(諛곗뿴) : " + Arrays.toString(date4));
				
					//5) list�뿉 �떞湲�
					List<String> dateList = new ArrayList<String>();
					for(String item1 : date4) {
						dateList.add(item1);
					}
				
					///------contentVo 媛� �꽔湲� 
				
				
					//content 媛� 諛쏄린
				
				
				
					//�솗�씤 
					System.out.println("dateList:"+ dateList);
				
					model.addAttribute("dateList", dateList);//list
				}
				
					//return mv;
					return "/board/read";
		
		
	}

	// 湲� �씫湲� + �뙎湲� �벑濡� �슂泥�
	@RequestMapping(value = "/contentRead/{cid}", method = RequestMethod.POST) // �럹�씠吏� 留곹겕 媛� c:url value="/board/read/${board.seq}" 湲� �씫湲�
	public String read(@PathVariable int cid, ReplyVO replyVO, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) { // �궗�슜�옄媛� �엯�젰�븳 媛� 以� ���엯�씠 留욎� �븡嫄곕굹 null 媛믪씤 寃쎌슦 �삁�쇅泥섎━
			return "/board/contentRead";
		}
		contentService.repInsert(replyVO);
		return "redirect:/contentRead/{cid}";
	}

	
	
	//Ajax �슂泥�諛쏅뒗 硫붿꽌�뱶 
		@RequestMapping(value="/content/getPersonNumber", method=RequestMethod.GET)
		@ResponseBody				//inputcode�뒗 �궇吏쒖� cid 
		public int getPersonNumber(String inputcode, String cid) {
			
//			String date = req.getParameter("inputcode");
//			int cid = Integer.parseInt(req.getParameter("cid"));
			int cid2 = Integer.parseInt(cid);
			
			System.out.println("inputcode: "+ inputcode);
			System.out.println("cid2: "+ cid2);
			
			
			//List(map<�궇吏�(String), �씤�썝�닔(int)>)  : 留듭쓣 由ъ뒪�듃濡� 諛쏆븘�샂 
			List<ResDays> resDays = boardService.getDayMap(cid2);
			//Map<String, Integer> map = new HashMap<String, Integer>(); --> �솢 Map �궗�슜�븳嫄곗빞�븘�븙
			for (ResDays res : resDays) {
				String key=res.getResday().substring(0, 10);
				int value=res.getPerson();
				//map.put(key, value);	
				System.out.println("key: "+ key +" value: "+ value);
				if (key.equals(inputcode)) {
					System.out.println("�씤�썝 : "+value);
					return value;
				}
		
			}
			return 0;
			
		}

	
	
	// �뙆�씪 �떎�슫濡쒕뱶 湲곕뒫
	@RequestMapping(value = "/down/{file_name}", method = RequestMethod.GET) // {file_name}濡� 諛쏆� 媛믪쑝濡� �씠由꾩씠 ���옣�맖..�솢???
	public void down(Model model, @PathVariable String file_name, HttpServletRequest request, HttpServletResponse response) {
		//String path =  request.getSession().getServletContext().getRealPath("���옣寃쎈줈");

		file_name = request.getParameter("fileName");
		String realFilename="";
		System.out.println(file_name);

		try {
			String browser = request.getHeader("User-Agent"); 
			//�뙆�씪 �씤肄붾뵫 
			if (browser.contains("MSIE") || browser.contains("Trident") || browser.contains("Chrome")) {
				file_name = URLEncoder.encode(file_name, "UTF-8").replaceAll("\\+", "%20");
			} else {
				file_name = new String(file_name.getBytes("UTF-8"), "ISO-8859-1");
			}
		} catch (UnsupportedEncodingException ex) {
			System.out.println("UnsupportedEncodingException");
		}
		realFilename = "D:\\file\\" + file_name;
		System.out.println(realFilename);

		File file1 = new File(realFilename);
		if (!file1.exists()) {
			return ;
		}

		// �뙆�씪紐� 吏��젙        
		response.setContentType("application/octer-stream");
		response.setHeader("Content-Transfer-Encoding", "binary;");
		response.setHeader("Content-Disposition", "attachment; file_name=\"" + file_name + "\"");
		try {
			OutputStream os = response.getOutputStream();
			FileInputStream fis = new FileInputStream(realFilename);

			int ncount = 0;
			byte[] bytes = new byte[512];

			while ((ncount = fis.read(bytes)) != -1 ) {
				os.write(bytes, 0, ncount);
			}
			fis.close();
			os.close();
		} catch (Exception e) {
			System.out.println("FileNotFoundException : " + e);
		}
	}
	
	@RequestMapping(value = "/imgRead/${cid}", method = RequestMethod.GET) // {file_name}濡� 諛쏆� 媛믪쑝濡� �씠由꾩씠 ���옣�맖..�솢???
	public void imgRead(Model model, @PathVariable int cid, String cthumbnail, HttpServletRequest request,HttpServletResponse response) {
		//String path =  request.getSession().getServletContext().getRealPath("���옣寃쎈줈");

		cthumbnail = request.getParameter("fileName");
		String realFilename="";
		System.out.println(cthumbnail);

		try {
			String browser = request.getHeader("User-Agent"); 
			//�뙆�씪 �씤肄붾뵫 
			if (browser.contains("MSIE") || browser.contains("Trident") || browser.contains("Chrome")) {
				cthumbnail = URLEncoder.encode(cthumbnail, "UTF-8").replaceAll("\\+", "%20");
			} else {
				cthumbnail = new String(cthumbnail.getBytes("UTF-8"), "ISO-8859-1");
			}
		} catch (UnsupportedEncodingException ex) {
			System.out.println("UnsupportedEncodingException");
		}
		realFilename = "D:\\file\\" + cthumbnail;
		System.out.println(realFilename);

		File file1 = new File(realFilename);
		if (!file1.exists()) {
			return ;
		}

		// �뙆�씪紐� 吏��젙        
		response.setContentType("application/octer-stream");
		response.setHeader("Content-Transfer-Encoding", "binary;");
		response.setHeader("Content-Disposition", "attachment; cthumbnail=\"" + cthumbnail + "\"");
		try {
			OutputStream os = response.getOutputStream();
			FileInputStream fis = new FileInputStream(realFilename);

			int ncount = 0;
			byte[] bytes = new byte[512];

			while ((ncount = fis.read(bytes)) != -1 ) {
				os.write(bytes, 0, ncount);
			}
			fis.close();
			os.close();
		} catch (Exception e) {
			System.out.println("FileNotFoundException : " + e);
		}
	}
	

	// �깉 湲� �옉�꽦 �슂泥�
	@RequestMapping(value = "/write", method = RequestMethod.GET)
	public String write(Model model) {
		model.addAttribute("contentVO", new ContentVO()); // Board 媛앹껜瑜� �깮�꽦�븯�뿬 Model�뿉 異붽��븯�뿬 媛앹껜媛� �뾾�쓣 �뻹 �삁�쇅 �젣嫄�
		return "/board/write";
	}

	// �깉 湲� �벑濡� �슂泥�
	@RequestMapping(value = "/write", method = RequestMethod.POST)
	public String write(ContentVO contentVO, BindingResult bindingResult) throws Exception {
		if (bindingResult.hasErrors()) { // �궗�슜�옄媛� �엯�젰�븳 媛� 以� ���엯�씠 留욎� �븡嫄곕굹 null 媛믪씤 寃쎌슦 �삁�쇅泥섎━
			return "/board/write";
		}
		
		/* 泥⑤� �뙆�씪 */
		String file_name = null;
		String cthumbnail = null;
		String pic_content = null;
		
		MultipartFile uploadFile = contentVO.getUploadFile();
		MultipartFile cthumbFile = contentVO.getCthumbFile();
		MultipartFile picFile = contentVO.getPicFile();
		
		System.out.println(uploadFile);
		System.out.println(cthumbFile);
		System.out.println(picFile);

		if (!uploadFile.isEmpty()) {
			String orgFileName = uploadFile.getOriginalFilename();
			String ext = FilenameUtils.getExtension(orgFileName);
			UUID uuid = UUID.randomUUID();
			file_name = uuid + "." + ext;
			uploadFile.transferTo(new File("D:\\file\\" + file_name));
			
		} else {
			file_name = "";
		}
		if (!cthumbFile.isEmpty()) {
			String orgFileName = cthumbFile.getOriginalFilename();
			String ext = FilenameUtils.getExtension(orgFileName);
			UUID uuid = UUID.randomUUID();
			cthumbnail = uuid + "." + ext;
			cthumbFile.transferTo(new File("D:\\file\\" + cthumbnail));
		} else {
			cthumbnail = "";
		}
		if (!picFile.isEmpty()) {
			String orgFileName = picFile.getOriginalFilename();
			String ext = FilenameUtils.getExtension(orgFileName);
			UUID uuid = UUID.randomUUID();
			pic_content = uuid + "." + ext;
			picFile.transferTo(new File("D:\\file\\" + pic_content));
			System.out.println(picFile);
		} else {
			pic_content = "";
		}

		contentVO.setFile_name(file_name);
		contentVO.setCthumbnail(cthumbnail);
		contentVO.setPic_content(pic_content);
		
		contentService.classInsert(contentVO);
		return "redirect:/";
	}

	// �닔�젙�븷 湲� �슂泥�
	@RequestMapping(value = "/edit/{cid}", method = RequestMethod.GET)
	public String edit(@PathVariable int cid, Model model) {
		ContentVO contentVO = contentService.select(cid);
		model.addAttribute("contentVO", contentVO);
		return "/board/edit";
	}

	// 湲� �닔�젙
	@RequestMapping(value = "/edit/{cid}", method = RequestMethod.POST)
	public String edit(ContentVO contentVO, BindingResult result, Model model, @PathVariable int cid) throws IOException {
		if (result.hasErrors()) {
			return "/board/edit";
		} else {
			
			String file_name = null;
			MultipartFile uploadFile = contentVO.getUploadFile();
			
			System.out.println(uploadFile);

			if (!uploadFile.isEmpty()) {
				String orgFileName = uploadFile.getOriginalFilename();
				String ext = FilenameUtils.getExtension(orgFileName);
				UUID uuid = UUID.randomUUID();
				file_name = uuid + "." + ext;
				uploadFile.transferTo(new File("D:\\file\\" + file_name));
			} else {
				file_name = "";
			}

			contentVO.setFile_name(file_name);

			contentService.update(contentVO);
			return "redirect:/";
		}
		
	}

	// 湲� �궘�젣 �슂泥��쓣 泥섎━�븷 硫붿꽌�뱶
	@RequestMapping(value = "/delete/{cid}", method = RequestMethod.GET)
	public String delete(@PathVariable int cid, Model model) {
		model.addAttribute("cid", cid);
		return "/board/delete";
	}

	@RequestMapping(value="/delete/{cid}", method = RequestMethod.POST)
	public String delete(@PathVariable int cid, String password, Model model) {
		int rowCount;
		ContentVO contentVO = new ContentVO();
		contentVO.setCid(cid);
		//contentVO.setPassword(password);

		rowCount = contentService.delete(contentVO);

		if(rowCount == 0) {
			model.addAttribute("cid", cid);
			//model.addAttribute("msg", "鍮꾨�踰덊샇媛� �씪移섑븯吏� �븡�뒿�땲�떎.");
			return "/board/delete";
		} else {
			return "redirect:/";
		}
	}
	
	// 愿�由ъ옄 �럹�씠吏�
	@RequestMapping(value = "/changeMenu", method=RequestMethod.POST)
    public String changeMenuUpload(@RequestParam(value="value")String value) {
       System.out.println(value);
       String value1 = "1";
       String value2 = "2";
       if(value.equals(value1)) {
       return "board/write";
       }
       else if(value.equals(value2)) {
       return "user";
       }
       return "managePage";
    }
    
    @RequestMapping(value="/managePage")
    public String managePage() {
       return "board/managePage";
    }

	// 湲� �궘�젣 �슂泥��쓣 泥섎━�븷 硫붿꽌�뱶

}

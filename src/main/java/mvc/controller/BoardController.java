package mvc.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;

import mvc.model.BoardDAO;
import mvc.model.BoardDTO;
import mvc.model.RippleDAO;
import mvc.model.RippleDTO;

@WebServlet("*.do")
public class BoardController extends HttpServlet {
	static final int LISTCOUNT = 5;	//
	private String boardName = "board";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);	//
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String RequestURI = request.getRequestURI();	// 
		String contextPath = request.getContextPath();	// 
		String command = RequestURI.substring(contextPath.length());	//
		
		response.setContentType("text/html; charset=utf-8");
		request.setCharacterEncoding("utf-8");
		
		System.out.println(command);
		
		if (command.contains("/BoardListAction.do")) {	// 
			requestBoardList(request);
			RequestDispatcher rd = request.getRequestDispatcher("../board/list.jsp");
			rd.forward(request,response);
		}
		else if (command.contains("/BoardWriteForm.do")) {	// 
			//requestLoginName(request);
			RequestDispatcher rd = request.getRequestDispatcher("../board/writeForm.jsp");
			rd.forward(request, response);
		}
		else if (command.contains("/BoardWriteAction.do")) {	
			try {
				requestBoardWrite(request);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			RequestDispatcher rd = request.getRequestDispatcher("../board/BoardListAction.do");
			rd.forward(request, response);
		}
		else if (command.contains("/BoardViewAction.do")) {		// ���õ� �� �� ������ ��������
			requestBoardView(request);
			requestRippleList(request);
			RequestDispatcher rd = request.getRequestDispatcher("../board/BoardView.do");
			rd.forward(request, response);
		}
		else if (command.contains("/BoardView.do")) {		// �� �� ������ ���
			RequestDispatcher rd = request.getRequestDispatcher("../board/view.jsp");
			rd.forward(request, response);
		}
		else if (command.contains("/BoardUpdateForm.do")) {		// �� ������ ���
			requestBoardView(request);
			RequestDispatcher rd = request.getRequestDispatcher("../board/updateForm.jsp");
			rd.forward(request, response);
		}
		else if (command.contains("/BoardUpdateAction.do")) {	// �� ����
			requestBoardUpdate(request);
			RequestDispatcher rd = request.getRequestDispatcher("../board/BoardListAction.do");
			rd.forward(request, response);
		}
		else if (command.contains("/BoardDeleteAction.do")) {	// // ���õ� �� ����
			requestBoardDelete(request);	
			RequestDispatcher rd = request.getRequestDispatcher("../board/BoardListAction.do");
			rd.forward(request, response);
		}
		else if (command.contains("/BoardRippleWriteAction.do")) {	// ��� �ۼ�
			requestBoardRippleWrite(request);
			String num = request.getParameter("num");
			String pageNum = request.getParameter("pageNum");
			response.sendRedirect("BoardViewAction.do?num=" + num + "&pageNum=" + pageNum);
		}
		else if (command.contains("/BoardRippleDeleteAction.do")) {	// ��� ����
			requestBoardRippleDelete(request);
			String num = request.getParameter("num");
			String pageNum = request.getParameter("pageNum");
			response.sendRedirect("BoardViewAction.do?num=" + num + "&pageNum=" + pageNum);
		}
		
		// ajax�� ����ó��
		else if (command.contains("RippleListAction.do")) {
			requestRippleList(request, response);
		}
		
		else if (command.contains("RippleWriteAction.do")) {
			requestRippleWrite(request, response);
		}
		
		else if (command.contains("RippleDeleteAction.do")) {	// ��� ����
			requestRippleDelete(request, response);
		}
		
		else {
			System.out.println("out : " + command);
			// ��� ȭ���� ��� ��Ʈ���� ���� ���
			PrintWriter out = response.getWriter();
			out.append("<html><body><h2>�߸��� ����Դϴ�.(" + command + "</h2><hr>");
		}
	}
	
	// ������ ����ڸ� ��������
	/*
	public void requestLoginName(HttpServletRequest request) {
		String id = request.getParameter("id");
		
		BoardDAO dao = BoardDAO.getInstance();
		
		String name = dao.getLoginNameById(id);
		
		request.setAttribute("name", name);
	}
	*/
	
	// 
	public void requestBoardWrite(HttpServletRequest request) throws Exception {
		BoardDAO dao = BoardDAO.getInstance();
		
		BoardDTO board = new BoardDTO();
		
		HttpSession session = request.getSession();
		board.setId((String) session.getAttribute("sessionId"));
		
		// �� ���������� ���۵� ������ ������ ������ ��Ѹ� �ۼ�
		String path=  "C:/img";
		
		// ���� ���ε带 ���� DiskFileUpload Ŭ������ ����
		DiskFileUpload upload = new DiskFileUpload();
		
		// ���ε��� ������ �ִ� ũ��, �޸𸮻� ������ �ִ� ũ��, ���ε�� ������ �ӽ÷� ������ ��θ� �ۼ�
		upload.setSizeMax(1000000);
		upload.setSizeThreshold(4096);
		upload.setRepositoryPath(path);
		
		// �� ���������� ���۵� ��û �Ķ���͸� ���޹޵��� DiskFileUpload ��ü Ÿ���� parseRequest() �޼��带 �ۼ�
		List items = upload.parseRequest(request);
		
		// �� ���������� ���۵� ��û �Ķ���͸� Iterator Ŭ������ ��ȯ
		Iterator params = items.iterator();
		
		while (params.hasNext()) {	// �� ���������� ���۵� ��û �Ķ���Ͱ� ���� ������ �ݺ��ϵ��� Iterator ��ü Ÿ���� hasNext() �޼��带 �ۼ�
			// �� ���������� ���۵� ��û �Ķ������ �̸��� ���������� Iterator ��ü Ÿ���� next() �޼��带 �ۼ�
			FileItem item = (FileItem) params.next();
			
			if (item.isFormField()) {
				// �� ���������� ���۵� ��û �Ķ���Ͱ� �Ϲ� �������̸� ��û �Ķ������ �̸��� ���� ���
				String name = item.getFieldName();
				String value = item.getString("utf-8");
				
				switch (name) {
					case "name":
						board.setName(value);
						break;
					case "subject":
						board.setSubject(value);
						break;
					case "content":
						board.setContent(value);
						break;
				}
				System.out.println(name + "=" + value + "<br>");
			}
			else {
				// �� ���������� ���۵� ��û �Ķ���Ͱ� �����̸�
				// ��û �Ķ������ �̸�, ���� ������ �̸�, ���� ������ ����, ���� ũ�⿡ ���� ������ ���
				String fileFieldName = item.getFieldName();
				String fileName = item.getName();
				String contentType = item.getContentType();
				
				if (!fileName.isEmpty()) {
					System.out.println("�����̸� : " + fileName);
					fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
					long fileSize = item.getSize();
					
					File file = new File(path + "/" + fileName);
					item.write(file);
					
					board.setFilename(fileName);
					board.setFilesize(fileSize);
					
					System.out.println("----------------------------<br>");
					System.out.println("��û �Ķ���� �̸� : " + fileFieldName + "<br>");
					System.out.println("���� ���� �̸� : " + fileName + "<br>");
					System.out.println("���� ������ Ÿ�� : " + contentType + "<br>");
					System.out.println("���� ũ�� : " + fileSize);
				}
			}
		}

		//java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy/MM/dd(HH:mm:ss)");
		//String regist_day = formatter.format(new java.util.Date());
		
		board.setHit(0);
		//board.setRegist_day(regist_day);
		board.setIp(request.getRemoteAddr());
		
		dao.insertBoard(board);	
	}
	
	//
	public void requestBoardView(HttpServletRequest request) {
		BoardDAO dao = BoardDAO.getInstance();
		int num = Integer.parseInt(request.getParameter("num"));
		int pageNum = Integer.parseInt(request.getParameter("pageNum"));
		
		BoardDTO board = new BoardDTO();
		board = dao.getBoardByNum(num, pageNum);
		
		request.setAttribute("num", num);
		request.setAttribute("page", pageNum);
		request.setAttribute("board", board);
	}
	
	// 
	public void requestBoardUpdate(HttpServletRequest request) {
		int num = Integer.parseInt(request.getParameter("num"));
		int pageNum = Integer.parseInt(request.getParameter("pageNum"));
		
		BoardDAO dao = BoardDAO.getInstance();
		
		BoardDTO board = new BoardDTO();
		board.setNum(num);
		board.setName(request.getParameter("name"));
		board.setSubject(request.getParameter("subject"));
		board.setContent(request.getParameter("content"));
		
		dao.updateBoard(board);
	}
	
	// 
	public void requestBoardDelete(HttpServletRequest request) {
		int num = Integer.parseInt(request.getParameter("num"));
		int pageNum = Integer.parseInt(request.getParameter("pageNum"));
		
		BoardDAO dao = BoardDAO.getInstance();
		dao.deleteBoard(num);
	}
	
	// 
	public void requestBoardList(HttpServletRequest request) {
		BoardDAO dao = BoardDAO.getInstance();
		List<BoardDTO> boardlist = new ArrayList<BoardDTO>();
		
		int pageNum = 1;	// 
		int limit=LISTCOUNT;	// 
		
		if(request.getParameter("pageNum")!=null)	//
			pageNum=Integer.parseInt(request.getParameter("pageNum"));
		
		String items = request.getParameter("items");	// 
		String text = request.getParameter("text");		//
		
		int total_record = dao.getListCount(items, text);	//
		boardlist = dao.getBoardList(pageNum, limit, items, text);	// 
		
		int total_page;
		
		if (total_record % limit == 0) {	// 
			total_page = total_record/limit;
			Math.floor(total_page);
		}
		else {
			total_page = total_record/limit;
			Math.floor(total_page);
			total_page = total_page + 1;
		}
		
		request.setAttribute("limit", limit);
		request.setAttribute("pageNum", pageNum);	// 
		request.setAttribute("total_page", total_page);	// 
		request.setAttribute("total_record", total_record);	// 
		request.setAttribute("boardlist", boardlist);
	}
	
	// ��� �ۼ�
	public void requestBoardRippleWrite(HttpServletRequest request) throws UnsupportedEncodingException {
		
		int num = Integer.parseInt(request.getParameter("num"));
		
		RippleDAO dao = RippleDAO.getInstance();
		RippleDTO ripple = new RippleDTO();
		
		request.setCharacterEncoding("utf-8");
		
		HttpSession session = request.getSession();
		ripple.setBoardName(this.boardName);
		ripple.setBoardNum(num);
		ripple.setMemberId((String) session.getAttribute("sessionId"));
		ripple.setName(request.getParameter("name"));
		ripple.setContent(request.getParameter("content"));
		ripple.setIp(request.getRemoteAddr());
		
		dao.insertRipple(ripple);
	}
	
	// ��� ��� ��������
	public void requestRippleList(HttpServletRequest request) {
		RippleDAO dao = RippleDAO.getInstance();
		List<RippleDTO> rippleList = new ArrayList<>();
		int num = Integer.parseInt(request.getParameter("num"));
		
		rippleList = dao.getRippleList(this.boardName, num);
		
		request.setAttribute("rippleList", rippleList);
	}
	
	// ��� ����
	public void requestBoardRippleDelete(HttpServletRequest request) throws UnsupportedEncodingException {
		
		int rippleId = Integer.parseInt(request.getParameter("rippleId"));
		
		RippleDAO dao = RippleDAO.getInstance();
		RippleDTO ripple = new RippleDTO();
		ripple.setRippleId(rippleId);
		dao.deleteRipple(ripple);
	}
	
	
	public void requestRippleList(HttpServletRequest request, HttpServletResponse response) throws IOException {
		request.setCharacterEncoding("UTF-8");
		
		HttpSession session = request.getSession();
		String sessionId = (String) session.getAttribute("sessionId");
		
		String boardName = request.getParameter("boardName");
		int num = Integer.parseInt(request.getParameter("num"));
		
		RippleDAO dao = RippleDAO.getInstance();
		ArrayList<RippleDTO> list = dao.getRippleList(boardName, num);
		
		StringBuilder result = new StringBuilder("{ \"listData\" :  [");
		int i = 0;
		for (RippleDTO dto : list) {
			boolean flag = sessionId != null && sessionId.equals(dto.getMemberId()) ? true : false;
			result.append("{\"rippleId\" : \"")
			.append(dto.getRippleId())
			.append("\", \"name\" : \"")
			.append(dto.getName())
			.append("\", \"content\" : \"")
			.append(dto.getContent())
			.append("\", \"isWriter\": \"")
			.append(flag)
			.append("\" }");
			// value�� �迭 ���·� ���� ������ ����� ��쿡�� �޸��� ������ �ȵ�
			
			if (i++ < list.size() - 1)
				result.append(", ");
		}
		result.append("]}");
		
		// ��� ȭ���� ��� ��Ʈ���� ���� ���
		PrintWriter out = response.getWriter();
		out.append(result.toString());
	}
	
	public void requestRippleDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		int rippleId = Integer.parseInt(request.getParameter("rippleId"));
		RippleDAO dao = RippleDAO.getInstance();
		RippleDTO ripple = new RippleDTO();
		ripple.setRippleId(rippleId);
		
		String result = "{ \"result\" : ";
		if (dao.deleteRipple(ripple)) {
			result += "\"true\"}";
		}
		else {
			result += "\"false\"}";
		}
		// ��� ȭ���� ��� ��Ʈ���� ���� ���
		PrintWriter out = response.getWriter();
		out.append(result);
		
	}
	
	public void requestRippleWrite(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		RippleDAO dao = RippleDAO.getInstance();
		RippleDTO ripple = new RippleDTO();
		HttpSession session = request.getSession();
		
		request.setCharacterEncoding("utf-8");
		
		ripple.setBoardName(request.getParameter("boardName"));
		ripple.setBoardNum(Integer.parseInt(request.getParameter("num")));
		ripple.setMemberId((String) session.getAttribute("sessionId"));
		ripple.setName(request.getParameter("name"));
		ripple.setContent(request.getParameter("content"));
		ripple.setIp(request.getRemoteAddr());
		
		String result = "{ \"result\" : ";
		if (dao.insertRipple(ripple)) {
			result += "\"true\"}";
		}
		else {
			result += "\"false\"}";
		}
		// ��� ȭ���� ��� ��Ʈ���� ���� ���
		PrintWriter out = response.getWriter();
		out.append(result);
		
	}
	
}

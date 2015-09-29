package tameD;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Scanner;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Kanri extends HttpServlet{
	private static final String TITLE = "掲示板-管理画面";
	private static final long serialVersionUID = 1L;
    private Oracle mOracle;

    //タグの無効化
    public static String CONVERT(String str)
    {
    	return
    		str.replaceAll("&","&amp;")
    		.replaceAll("<","&gt;")
    		.replaceAll(">","&lt;")
    		.replaceAll("\n","<br>");
    }
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Kanri() {
        super();
        // TODO Auto-generated constructor stub
    }
	@Override
	public void init() throws ServletException {
		// TODO 自動生成されたメソッド・スタブ
		super.init();


		try{
			ServletContext context = getServletConfig().getServletContext();
			URL resource = context.getResource("/WEB-INF/db.txt");
			InputStream stream = resource.openStream();
			Scanner sc = new Scanner(stream);
			String id = sc.next();
			String pass = sc.next();
			sc.close();
			stream.close();

			mOracle = new Oracle();
			mOracle.connect("ux4", id, pass);

			} catch (Exception e) {
			System.err.println("db.txtにユーザ情報が設定されていない、もしくは認証に失敗しました");
		}
	}

	@Override
	public void destroy() {
		//DB切断
		mOracle.close();
		// TODO 自動生成されたメソッド・スタブ
		super.destroy();
	}



	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		action(request,response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		action(request,response);
	}

	protected void action(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 要求文字コードのセット(Javaプログラムからはき出す文字コード)
        response.setCharacterEncoding("UTF-8");
        // 応答文字コードのセット(クライアントに通知する文字コードとファイルの種類)
        response.setContentType("text/html; charset=UTF-8");

        // 出力ストリームの取得
        PrintWriter out = response.getWriter();


        //テンプレートファイルを読む
        Keijiban ka = new Keijiban();
        ka.open(this, "Kanri.html");
        //タイトルの置換
        ka.replace("$(TITLE)", TITLE);

        //各ページの読み込み
        Keijiban p1 = new Keijiban();
        p1.open(this, "saku.html");
        Keijiban p2 = new Keijiban();
        p2.open(this, "sakuse.html");
        Keijiban p3 = new Keijiban();
        p3.open(this, "tuketu.html");
        Keijiban p4 = new Keijiban();
        p4.open(this, "index.html");

        //パラメータによって内容を切り替え
        String param1 = request.getParameter("k");
        if (param1 != null && param1.length() > 0)
        {
        	int index =  Integer.parseInt(param1);
        	if(index == 1)
        		ka.replace("$(PAGE)", p1.getText());
        	else if(index == 2)
        		ka.replace("$(PAGE)", p2.getText());
        	else if(index == 3)
        		ka.replace("$(PAGE)", p3.getText());
        	else if(index == 4)
        		ka.replace("$(PAGE)", p4.getText());
        }
        else



        //内容の出力
        out.print(ka.getText());
        //出力終了
        out.close();
	}


}

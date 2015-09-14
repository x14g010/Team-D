package tameD;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class system
 */
@WebServlet("/system")
public class system extends HttpServlet {
	private static final String TITLE = "";
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
    public system() {
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

			//テーブルが無ければ作成
			if(!mOracle.isTable("exam01"))
				mOracle.execute("create table exam01(msg varchar(200))");
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

        //パラメータにデータがあった場合はDBへ挿入
        String param1 = request.getParameter("data1");
        if (param1 != null && param1.length() > 0)
        {
        	//UTF8をJava文字列に変換
        	String data1 = new String(param1.getBytes("ISO-8859-1"),"UTF-8");
        	//SQL文の作成 Oracle.STRはシングルクオートのエスケープ処理
        	String sql = String.format("insert into exam01 values('%s')",Oracle.STR(data1));
        	//デバッグ用
        	System.out.println("DEBUG:SQL文 "+sql);
        	//DBにSQL文を実行させる
        	mOracle.execute(sql);
        }
        //テンプレートファイルを読む
        Keijiban ts = new Keijiban();
        ts.open(this, "Template.html");
        //タイトルの置換
        ts.replace("$(TITLE)", TITLE);

        //文字列保存用バッファの作成
        StringBuilder sb = new StringBuilder();

        //データの抽出
        try {
			ResultSet res = mOracle.query("select * from exam01");
			while(res.next())
			{
				String data = res.getString(1);
				if(data != null)
				{
					//文字列バッファにメッセージ内容を貯める
					//CONVERTはタグの無効化
					sb.append(String.format("<hr>%s<BR>\n", CONVERT(data)));
				}
			}
			//メッセージの置換
	        ts.replace("$(MSG)", sb.toString());
		} catch (SQLException e) {}

        //内容の出力
        out.print(ts.getText());
        //出力終了
        out.close();
	}

}

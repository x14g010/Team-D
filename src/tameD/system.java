package tameD;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
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
	private static final String TITLE = "掲示板";
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
			if(!mOracle.isTable("com")){
				mOracle.execute("create sequence seq");
				mOracle.execute("create table com(comID number,usNAME varchar2(50),usID number"
								+ ",comDATE DATE,comMSG varchar(200))");
			}
			if(!mOracle.isTable("genre"))
				mOracle.execute("create table genre(genID number,genNAME varchar2(50))");
			if(!mOracle.isTable("kiji"))
				mOracle.execute("create table kiji(kijiID number,kijiTITLE varchar2(100),kijiMSG varchar(200),"
								+ "kijiDATE DATE)");

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
        Keijiban ts = new Keijiban();
        ts.open(this, "index.html");
        //タイトルの置換
        ts.replace("$(TITLE)", TITLE);

        //文字列保存用バッファの作成
        StringBuilder sb = new StringBuilder();
        //データの抽出
        try {
			//ジャンル読み込み
        	ResultSet gen = mOracle.query("select * from genre");
			while(gen.next())
			{
				int id =gen.getInt(1);
				String data = gen.getString(2);

				if(data != null)
				{
					//文字列バッファにメッセージ内容を貯める
					//CONVERTはタグの無効化
					sb.append(String.format("<hr><a href=\"?j=%d\">%s</a><br>", id,CONVERT(data)));
				}
			}
			//メッセージの置換
	        ts.replace("$(GENRE)", sb.toString());
	        //ジャンル選択時、ページの読み込み
	        Keijiban p1 = new Keijiban();
	        p1.open(this, "kijise.html");
	      //パラメータによって内容を切り替え
	        String param1 = request.getParameter("j");
	        if (param1 != null && param1.length() > 0)
	        {
	        	int index =  Integer.parseInt(param1);
	        	if(index == 1)
	        		ts.replace("$(PAGE)", p1.getText());
	        }
	        else{
        }


	        //ジャンル記事読み込み
	        ResultSet genk = mOracle.query("select * from kiji");
			while(genk.next())
			{
				String data = genk.getString(2);
				//日付受け取り
				Calendar cal = Calendar.getInstance();
				cal.setTime(genk.getDate(4));
				if(data != null)
				{
					//文字列バッファにメッセージ内容を貯める
					//CONVERTはタグの無効化
					sb.append(String.format("<hr>%s:%d年%d月%d日 %d時%d分<br>", CONVERT(data),
							cal.get(Calendar.YEAR),
							cal.get(Calendar.MONTH)+1,
							cal.get(Calendar.DAY_OF_MONTH),
							cal.get(Calendar.HOUR_OF_DAY),
							cal.get(Calendar.MINUTE)));
				}
			}
			//メッセージの置換
	        ts.replace("$(PAGE)", sb.toString());

	        //記事コメント
	      //パラメータにデータがあった場合はDBへ挿入
	        String param2 = request.getParameter("data1");
	        if (param1 != null && param1.length() > 0)
	        {
	        	//UTF8をJava文字列に変換
	        	String data1 = new String(param1.getBytes("ISO-8859-1"),"UTF-8");
	        	//SQL文の作成 Oracle.STRはシングルクオートのエスケープ処理
	        	String sql = String.format("insert into com values(seq.nextval,'%s','%s',sysdate)",Oracle.STR(data1));
	        	//デバッグ用
	        	System.out.println("DEBUG:SQL文 "+sql);
	        	//DBにSQL文を実行させる
	        	mOracle.execute(sql);
	        }
	      //データの抽出
	        try {
				ResultSet res = mOracle.query("select * from com");
				while(res.next())
				{
					String data = res.getString(5);
					//日付の受け取り
					Calendar cal = Calendar.getInstance();
					cal.setTime(res.getDate(4));
					if(data != null)
					{
						//文字列バッファにメッセージ内容を貯める
						//CONVERTはタグの無効化
						sb.append(String.format("<hr>%d:%s:%d:%d年%d月%d日 %d時%d分<br>%s<BR>\n",
												cal.get(Calendar.YEAR),
												cal.get(Calendar.MONTH)+1,
												cal.get(Calendar.DAY_OF_MONTH),
												cal.get(Calendar.HOUR_OF_DAY),
												cal.get(Calendar.MINUTE),
												 CONVERT(data)));
					}
				}
				//メッセージの置換
		        ts.replace("$(COM)", sb.toString());
			} catch (SQLException e) {}

	      //選択時、ページの読み込み
	        Keijiban k1 = new Keijiban();
	        k1.open(this, "kiji.html");

	        //管理者画面へ


		} catch (SQLException e) {}

        //内容の出力
        out.print(ts.getText());
        //出力終了
        out.close();
	}

}

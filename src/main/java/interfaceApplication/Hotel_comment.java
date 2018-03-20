package interfaceApplication;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import common.java.JGrapeSystem.rMsg;
import common.java.apps.appsProxy;
import common.java.authority.plvDef;
import common.java.cache.CacheHelper;
import common.java.interfaceModel.GrapeDBSpecField;
import common.java.interfaceModel.GrapeTreeDBModel;
import common.java.session.session;
import common.java.string.StringHelper;

public class Hotel_comment {
	private CacheHelper caches;
	private session se;
	private JSONObject usersInfo;
	private String GrapeSid;
	private String currentWeb;
	private String userName;
	private String userId;
	private int userType;
	private String pkString;
	private String ugid;
	private GrapeTreeDBModel hotel_comment;
	private GrapeDBSpecField gDbSpecField;
	private String msg = rMsg.netMSG(0, "");
	private String uid;

	public Hotel_comment() {

		hotel_comment = new GrapeTreeDBModel();
		gDbSpecField = new GrapeDBSpecField();
		gDbSpecField.importDescription(appsProxy.tableConfig("hotel_comment"));
		hotel_comment.descriptionModel(gDbSpecField);
		hotel_comment.bindApp();
		pkString = hotel_comment.getPk();

		caches = new CacheHelper();
		se = new session();
		usersInfo = se.getDatas();
		GrapeSid = session.getSID();
		if (usersInfo != null && usersInfo.size() != 0) {
			currentWeb = usersInfo.getString("currentWeb"); // 当前用户所属网站id
			userName = usersInfo.getString("name"); // 当前用户姓名
			userId = usersInfo.getString("id"); // 当前用户用户名
			userType = usersInfo.getInt("userType");
			ugid = usersInfo.getString("ugid");
			uid = usersInfo.getString(pkString);//用户主键id
		}
	}

	/**
	 * TODO(屏蔽评论)
	 * 
	 * @param 评论IDs
	 * @return
	 */
	public String kickEvaluate(String eids) {
		hotel_comment.enableCheck();
		if (userType != plvDef.UserMode.root) {
			return rMsg.netMSG(99, "账号权限不对,需要管理员权限");
		}
		ArrayList<String> arrayList = new ArrayList<String>();
		if (StringHelper.InvaildString(eids)) {
			String[] eids_arr = eids.split(",");
			for (String eid : eids_arr) {
				boolean updateEx = hotel_comment.eq(pkString, eid).eq("deleteable", 0).hide();
				if (!updateEx) {
					arrayList.add(eid);
				}
			}
		} else {
			return rMsg.netMSG(99, "非法参数");
		}
		if (arrayList.size() == 0) {
			return rMsg.netMSG(0, "更新成功");
		} else {
			return rMsg.netMSG(1, "更新失败的有以下   " + arrayList);
		}
	}

	/**
	 * TODO(提交评论)
	 * 
	 * @param hid
	 *            酒店ID
	 * @param text
	 *            评论内容
	 * @return
	 */
	public String submitEvaluate(String hid, String text) {
		if (!StringHelper.InvaildString(uid)) {
			return rMsg.netMSG(99, "用户未登录");
		}
		JSONObject o = JSONObject.toJSON(text);
		if (o == null || o.size() < 1) {
			return rMsg.netMSG(98, "json非法");
		}

		if (!StringHelper.InvaildString(hid)) {
			return rMsg.netMSG(97, "酒店id为空");
		} else {
			Hotel hotel = new Hotel();
			String find = hotel.find(hid);
			if (find == null) {
				return rMsg.netMSG(96, "酒店id不存在");
			}
		}
		o.puts("hid", hid);
		Object insertEx = hotel_comment.data(o).autoComplete().insertEx();
		if (insertEx == null) {
			return rMsg.netMSG(1, "提交失败");
		} else {
			return rMsg.netMSG(0, "提交成功");
		}

	}

	/**
	 * TODO(获取对应酒店的评价量最大关键字)
	 * 
	 * @param hid
	 * @return
	 */
	public String submitEvaluate(String hid) {
		return null;
	}

}

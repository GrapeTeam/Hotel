package interfaceApplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import common.java.Concurrency.distributedLocker;
import common.java.JGrapeSystem.rMsg;
import common.java.apps.appsProxy;
import common.java.authority.objectPower;
import common.java.authority.plvDef;
import common.java.authority.plvDef.plvType;
import common.java.cache.CacheHelper;
import common.java.database.dbFilter;
import common.java.interfaceModel.GrapeDBSpecField;
import common.java.interfaceModel.GrapeTreeDBModel;
import common.java.session.session;
import common.java.string.StringHelper;
import unit.CollectionsUtil;
import unit.Distance;
import unit.PageUtil;
import common.java.interfaceType.apiType;

/**
 * @ClassName: Hotel
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author longitude 酒店经度 latitude 酒店纬度
 * @date 2018年3月14日 上午10:19:50
 * 
 */
public class Hotel {
	private GrapeTreeDBModel hotel;
	private GrapeDBSpecField gDbSpecField;
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
	private String netMSG;

	public Hotel() {
		netMSG = rMsg.netMSG(0, "");
		hotel = new GrapeTreeDBModel();
		gDbSpecField = new GrapeDBSpecField();
		gDbSpecField.importDescription(appsProxy.tableConfig("hotel"));
		hotel.descriptionModel(gDbSpecField);
		hotel.bindApp();
		pkString = hotel.getPk();

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
		}
	}

	/**
	 * TODO(恢复酒店，让该酒店可以显示在页面前端; 管理员权限下恢复任意酒店; 酒店管理员权限下恢复自己所经营的酒店)
	 * 
	 * @param 酒店IDs
	 * @return
	 */
	public String resumeHotel(String hids) {
		hotel.enableCheck();

		ArrayList<String> arrayList = new ArrayList<String>();
		if (StringHelper.InvaildString(hids)) {
			String[] hids_arr = hids.split(",");
			for (String hid : hids_arr) {
				boolean updateEx = hotel.eq(pkString, hid).show();
				if (!updateEx) {
					arrayList.add(hid);
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
	 * TODO(获得酒店列表（按距离最近最火的条件）; 管理员权限下可以看到全部酒店信息; 未登录或者用户权限下仅可以看到未被冻结的酒店信息)
	 * 
	 * @param longitude
	 *            经度
	 * @param latitude
	 *            纬度
	 * @param raidus
	 *            半径
	 * @param hotORnearORscore
	 *            排序选项
	 * @param sort
	 *            顺序:1 逆序:-1
	 * @return
	 */
	public String listHotel(double longitude, double latitude, int raidus, String hotORnearORscore, int sort) {
		hotel.enableCheck();

		if (userType == plvDef.UserMode.root) {
		}
		if (userType != plvDef.UserMode.root) {
			hotel.eq("visable", 1);
		}
		netMSG = getAreaHotel_sort(longitude, latitude, raidus, null, hotORnearORscore, sort);

		return netMSG;

	}

	/**
	 * TODO(冻结酒店，让该酒店无法显示在页面前端 ;管理员权限下冻 结任意酒店; 酒店管理员权限下冻结自己所经营的酒店)
	 * 
	 * @param 酒店IDs
	 * @return
	 */
	public String kickHotel(String hids) {
		hotel.enableCheck();

		ArrayList<String> arrayList = new ArrayList<String>();
		if (StringHelper.InvaildString(hids)) {
			String[] hids_arr = hids.split(",");
			for (String hid : hids_arr) {
				boolean updateEx = hotel.eq(pkString, hid).hide();
				if (!updateEx) {
					arrayList.add(hid);
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
	 * TODO(删除酒店; 需要管理员权限)
	 * 
	 * @param 酒店ID
	 * @return
	 */
	public String deleteHotel(String hids) {
		hotel.enableCheck();

		ArrayList<String> arrayList = new ArrayList<String>();
		if (StringHelper.InvaildString(hids)) {
			String[] hids_arr = hids.split(",");
			for (String hid : hids_arr) {
				boolean updateEx = hotel.eq(pkString, hid).deleteEx();
				if (!updateEx) {
					arrayList.add(hid);
				}
			}
		} else {
			return rMsg.netMSG(99, "非法参数");
		}

		if (arrayList.size() == 0) {
			return rMsg.netMSG(0, "删除成功");
		} else {
			return rMsg.netMSG(1, "删除失败的有以下   " + arrayList);
		}

	}

	/**
	 * TODO(更新酒店数据; 在管理员权限下可以更改所有信息; 在酒店管理员权限下无法修改酒店名称)
	 * 
	 * @param 基本酒店数据
	 * @return
	 */
	public String updateHotel(String data) {
		hotel.enableCheck();

		boolean update = false;
		JSONObject json = JSONObject.toJSON(data);
		if (json != null) {
			String _id = json.getString(pkString);
			if (StringHelper.InvaildString(_id)) {
				if (userType == plvDef.UserMode.root) {
				}
				if (userType == plvDef.UserMode.admin) {
					json.remove("name");
				}
				update = hotel.eq(pkString, _id).data(json).updateEx();
			}
		}
		return rMsg.netMSG(0, update);
	}

	/**
	 * TODO(新增酒店数据 管理员账号特有)
	 * 
	 * @param 基本酒店数据
	 * @return
	 */
	public String insertHotel(String data) {
		if (userType != plvDef.UserMode.root) {
			return rMsg.netMSG(99, "账号权限不对");
		}
		Object ob = null;
		JSONObject jsonObj = JSONObject.toJSON(data);
		if (jsonObj != null) {
			String ugid1 = jsonObj.getString("ugid");
			String o = (String) appsProxy.proxyCall("/GrapeUser/roles/find/" + ugid1);
			JSONObject json = JSONObject.toJSON(o);
			if (json.size() < 1) {
				return rMsg.netMSG(98, "用户组id不存在,请先添加用户组");
			}

			JSONObject rMode = new JSONObject(plvType.chkType, plvType.powerVal).puts(plvType.chkVal, 100);// 设置默认查询权限
			JSONObject uMode = new JSONObject(plvType.chkType, plvType.groupOwn).puts(plvType.chkVal, ugid1);
			JSONObject dMode = new JSONObject(plvType.chkType, plvType.groupOwn).puts(plvType.chkVal, ugid1);
			jsonObj.puts("rMode", rMode.toJSONString()); // 添加默认查看权限
			jsonObj.puts("uMode", uMode.toJSONString()); // 添加默认修改权限
			jsonObj.puts("dMode", dMode.toJSONString()); // 添加默认删除权限
			ob = hotel.data(jsonObj).autoComplete().insert();
		}
		return rMsg.netMSG(ob != null, (String) ob);
	}

	/**
	 * TODO(获取对应条件的酒店数据)
	 * 
	 * @param 城市编码
	 * @param 行政区编码
	 * 
	 * @param 各类基本条件的筛选
	 * @return
	 */
	public String getHotelList(long cityid, long areaid, String cond) {
		hotel.enableCheck();

		JSONArray jsonArray = JSONArray.toJSONArray(cond);
		hotel.eq("city", cityid).eq("area", areaid);
		if (jsonArray != null) {
			hotel.where(jsonArray);
		}
		JSONArray select = hotel.select();
		return rMsg.netMSG(0, select);

	}

	/**
	 * TODO(获取对应坐标附近的酒店的列表)
	 * 
	 * @param longitude
	 *            经度
	 * @param latitude
	 *            纬度
	 * @param raidus
	 *            半径米
	 * @param idx
	 *            页码
	 * @param pagesize
	 *            最大条目数
	 * @param cond
	 *            各类基本条件的筛选
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String getAreaHotel(double longitude, double latitude, int raidus, String cond) {
		return getAreaHotel_sort(longitude, latitude, raidus, cond, "dx", 1);

	}

	@SuppressWarnings("unchecked")
	public String choose_hotel(String hid) {
		se.push("hid", hid);

		return "";

	}

	@SuppressWarnings("unchecked")
	String getAreaHotel_sort(double longitude, double latitude, int raidus, String cond, String sortfeild, int sort) {
		if (raidus > 10000) {
			return rMsg.netMSG(98, "半径不能超过10公里");
		}
		if (Math.abs(longitude) > 180 || Math.abs(latitude) > 90 || raidus < 0) {
			return rMsg.netMSG(99, "参数不对");
		}
		JSONArray select = getAreaHotel_JSONArray(longitude, latitude, raidus, cond);
		CollectionsUtil.sort_double(select, sortfeild, sort);

		return rMsg.netMSG(0, select);

	}

	// @SuppressWarnings("unchecked")
	// String getAreaHotel_pageANDsort(double longitude, double latitude, int
	// raidus, int idx, int pagesize, String cond, String sortfeild, int sort) {
	// if (raidus > 10000) {
	// return rMsg.netMSG(98, "半径不能超过10公里");
	// }
	// if (Math.abs(longitude) > 180 || Math.abs(latitude) > 90 || raidus < 0) {
	// return rMsg.netMSG(99, "参数不对");
	// }
	// JSONArray select = getAreaHotel_JSONArray(longitude, latitude, raidus, cond);
	// CollectionsUtil.sort_double(select, sortfeild, sort);
	//
	// JSONArray page = (JSONArray) PageUtil.page(select, idx, pagesize);
	// return rMsg.netMSG(0, page);
	//
	// }

	JSONArray getAreaHotel_JSONArray(double longitude, double latitude, int raidus, String cond) {
		double[] around = Distance.getAround(longitude, latitude, raidus);
		double minLat = around[0];
		double minLng = around[1];
		double maxLat = around[2];
		double maxLng = around[3];
		dbFilter dbFilter = new dbFilter();
		dbFilter.gte("longitude", minLng).lte("longitude", maxLng).gte("latitude", minLat).lte("latitude", maxLat);
		JSONArray build = dbFilter.build();
		hotel.and().where(build);
		JSONArray jsonArray = JSONArray.toJSONArray(cond);
		if (jsonArray.size() > 0) {
			hotel.and().where(jsonArray);
		}
		JSONArray select = hotel.select();
		for (Object object : select) {
			JSONObject obj = (JSONObject) object;
			double longitude1 = (double) obj.get("longitude");
			double latitude1 = (double) obj.get("latitude");
			double dx = Distance.distanceByLongNLat(longitude, latitude, longitude1, latitude1);
			obj.puts("dx", dx);
			long evaluate = (long) obj.getLong("evaluate");
			long orderCnt = (long) obj.getLong("orderCnt");
			double hot = evaluate * 0.3 + orderCnt * 0.7;
			obj.puts("hot", hot);
			double score = (double) obj.get("score");
			obj.puts("score", score);
		}
		return select;
	}

}

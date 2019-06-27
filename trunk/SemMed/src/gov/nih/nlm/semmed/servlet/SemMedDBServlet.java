package gov.nih.nlm.semmed.servlet;

import gov.nih.nlm.semmed.model.SemMedDocument;
import gov.nih.nlm.semmed.struts.action.SearchAction;
import gov.nih.nlm.semmed.util.ArticleDataSource;
import gov.nih.nlm.semmed.util.MedlineSource;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.*;
import javax.servlet.*;
import javax.naming.NamingException;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Cookie;
import javax.sql.DataSource;

import org.json.*;

/**
 *
 * @author rodriguezal
 *
 */
public class SemMedDBServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(SemMedDBServlet.class);

	private DataSource ds;

	private String qryTemp = new String("select * from PREDICATION_AGGREGATE where PMID in ($1) limit 50000");
	private String qry2Temp = new String("select * from SENTENCE where PMID in ($1) order by PMID, TYPE desc, NUMBER");
	private String qry3Temp = new String("select * from SENTENCE where SENTENCE_ID in ($1) order by PMID, NUMBER");

	@Override
	public void init() throws ServletException  {
		try {
			Context ctx = new InitialContext();
	        ds =
	            (DataSource)ctx.lookup("java:comp/env/jdbc/SemMedDB");
			log.debug("SemMedDBServlet initialized.");
			// s = con.createStatement();
			/* String dbquery = qryTemp.replace("$1", "\'1001\'");
			ResultSet rs = s.executeQuery(dbquery);
			log.debug("result set obtained");
			while (rs.next()){
				log.debug(rs.getString("PMID") + ", " + rs.getString("predicate"));
			} */
		} catch(NamingException ne) {
			ne.printStackTrace();
		}
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		// log.debug("Getting request from SemMed2");
		doPost(req,resp);
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		Connection con = null;
		Statement s = null;
		ResultSet rs = null;
		try {
			log.debug("Getting request from SemMed2");
			// Statement s = con.createStatement();
			String ping =  req.getParameter("ping");
			if(ping != null) {
				log.debug("Getting ping request;");
				return;
			}
			String qry = req.getParameter("qry");
			String pmids = req.getParameter("PMIDs");
			String sids = req.getParameter("SIDs");
			String logout = req.getParameter("logout");
			log.debug("qry = " + qry);
			log.debug("PMIDs = " + pmids);
			log.debug("SIDs = " + sids);
			log.debug("logout = " + logout); 

			if(qry != null && !qry.equals("")) {
				con = ds.getConnection();
				s = con.createStatement();
				String dbquery = qryTemp.replace("$1", qry);

				rs = s.executeQuery(dbquery);
				JSONArray result = new JSONArray();
				 ArrayList<Predication> predArray = new ArrayList<Predication>();
				while (rs.next()){
                    Predication apred = new Predication();
                    apred.PID = rs.getString(1);
                    apred.SID = rs.getString(2);
                    apred.PNUMBER = rs.getString(3);
                    apred.PMID = rs.getString(4);
                    apred.predicate = rs.getString(5);
                    String temp_scui = rs.getString(6);
                    if (temp_scui.contains((CharSequence)"|")) {
                        if (temp_scui.startsWith("C")) {
                            log.debug((Object)("prev s_cui = " + temp_scui));
                            String[] compo1 = temp_scui.split("\\|");
                            apred.s_cui = compo1[0];
                            log.debug((Object)("cur s_cui = " + apred.s_cui));
                            String[] compo2 = rs.getString(7).split("\\|");
                            apred.s_name = compo2[0];
                            String[] compo3 = rs.getString(8).split("\\|");
                            apred.s_type = compo3[0];
                            apred.s_novel = rs.getString(9);
                        } else {
                            log.debug((Object)("prev s_cui = " + temp_scui));
                            apred.s_cui = temp_scui.replaceAll("\\|", ",");
                            log.debug((Object)("cur s_cui = " + apred.s_cui));
                            String temp_sname = rs.getString(7);
                            apred.s_name = temp_sname.replaceAll("\\|", ",");
                            String temp_stype = rs.getString(8);
                            apred.s_type = temp_stype.replaceAll("\\|", ",");
                            apred.s_novel = rs.getString(9);
                        }
                    } else {
                        apred.s_cui = rs.getString(6);
                        apred.s_name = rs.getString(7);
                        apred.s_type = rs.getString(8);
                        apred.s_novel = rs.getString(9);
                    }
                    String temp_ocui = rs.getString(10);
                    if (temp_ocui.contains((CharSequence)"|")) {
                        if (temp_ocui.startsWith("C")) {
                            log.debug((Object)("prev o_cui = " + temp_ocui));
                            String[] compo1 = temp_ocui.split("\\|");
                            apred.o_cui = compo1[0];
                            log.debug((Object)("cur_ocui = " + apred.o_cui));
                            String[] compo2 = rs.getString(11).split("\\|");
                            apred.o_name = compo2[0];
                            String[] compo3 = rs.getString(12).split("\\|");
                            apred.o_type = compo3[0];
                            apred.o_novel = rs.getString(13);
                        } else {
                            log.debug((Object)("prev o_cui = " + temp_ocui));
                            apred.o_cui = temp_ocui.replaceAll("\\|", ",");
                            log.debug((Object)("cur_ocui = " + apred.o_cui));
                            String temp_oname = rs.getString(11);
                            apred.o_name = temp_oname.replaceAll("\\|", ",");
                            String temp_otype = rs.getString(12);
                            apred.o_type = temp_otype.replaceAll("\\|", ",");
                            apred.o_novel = rs.getString(13);
                        }
                    } else {
                        apred.o_cui = rs.getString(10);
                        apred.o_name = rs.getString(11);
                        apred.o_type = rs.getString(12);
                        apred.o_novel = rs.getString(13);
                    }
                    predArray.add(apred);
                }
                for (int i = 0; i < predArray.size(); ++i) {
                    JSONArray ja = new JSONArray();
                    Predication apred = (Predication)predArray.get(i);
                    ja.put((Object)apred.PID);
                    ja.put((Object)apred.SID);
                    ja.put((Object)apred.PNUMBER);
                    ja.put((Object)apred.PMID);
                    ja.put((Object)apred.predicate);
                    ja.put((Object)apred.s_cui);
                    ja.put((Object)apred.s_name);
                    ja.put((Object)apred.s_type);
                    ja.put((Object)apred.s_novel);
                    ja.put((Object)apred.o_cui);
                    ja.put((Object)apred.o_name);
                    ja.put((Object)apred.o_type);
                    ja.put((Object)apred.o_novel);
                    result.put((Object)ja);
                    log.debug((Object)ja.toString());
                }
				// log.debug("select operation completed");
				s.close();
				rs.close();
				resp.setContentType("text/plain");
				ServletOutputStream outStream = resp.getOutputStream();
				outStream.print(result.toString());
			} else if(pmids != null && !pmids.equals("")) {
				if(pmids.endsWith(",")) {
					// log.debug("*** odd pmids = " + pmids);
					pmids = pmids.substring(0, pmids.length()-1);
				}
				String dbquery = qry2Temp.replace("$1", pmids);
				// log.debug("dbquery = " + dbquery);
				con = ds.getConnection();
				s = con.createStatement();
				rs = s.executeQuery(dbquery);
				JSONArray result = new JSONArray();
				while (rs.next()){
					String[] aRow = new String[5];
					JSONArray ja = new JSONArray();
					ja.put(rs.getString(1));
					ja.put(rs.getString(2));
					ja.put(rs.getString(3));
					ja.put(rs.getString(4));
					ja.put(rs.getString(5));
					result.put(ja);
					// log.debug(ja.toString());
				}
				// log.debug("select operation completed");
				s.close();
				rs.close();
				resp.setContentType("text/plain");
				ServletOutputStream outStream = resp.getOutputStream();
				outStream.print(result.toString());
			} else if(sids != null && !sids.equals("")) {
				if(sids.endsWith(",")) {
					log.debug("*** odd sids = " + sids);
					sids = sids.substring(0, sids.length()-1);
				}
				String dbquery = qry3Temp.replace("$1", sids);
				// log.debug("dbquery = " + dbquery);
				con = ds.getConnection();
				s = con.createStatement();
				rs = s.executeQuery(dbquery);
				JSONArray result = new JSONArray();
				while (rs.next()){
					String[] aRow = new String[5];
					JSONArray ja = new JSONArray();
					ja.put(rs.getString(1));
					ja.put(rs.getString(2));
					ja.put(rs.getString(3));
					ja.put(rs.getString(4));
					ja.put(rs.getString(5));
					result.put(ja);
					// log.debug(ja.toString());
				}
				// log.debug("select operation completed");
				s.close();
				rs.close();
				resp.setContentType("text/plain");
				ServletOutputStream outStream = resp.getOutputStream();
				outStream.print(result.toString());
			} else if(logout  != null && !logout.equals("") ) {
				log.debug("get cookie info");
				Cookie[] cookies = req.getCookies();
				HttpSession session = req.getSession();
				session.invalidate();
		        for(int i = 0; i< cookies.length ; ++i){
		        	log.debug("Cookie name = " + cookies[i].getName());
		        	if(cookies[i].getName().trim().compareTo("JSESSIONID") == 0){		            
		        		log.debug("Found Cookie name = " + cookies[i].getName());
		                //response.addCookie(cookie);
		        		cookies[i].setValue(null);
		                cookies[i].setMaxAge(0);
		                resp.addCookie(cookies[i]);
		            }
		        } 
				RequestDispatcher rd = req.getRequestDispatcher("logout.html");
				rd.forward(req, resp);
			}
			// log.debug(result.toString());

		} catch(Exception e) {
			e.printStackTrace();
		}
			finally {
	        if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
	        if (s != null) try { s.close(); } catch (SQLException ignore) {}
	        if (con != null) try { con.close(); } catch (SQLException ignore) {}
	    }
	}
	
	class Predication {
	    String PID;
	    String SID;
	    String PNUMBER;
	    String PMID;
	    String predicate;
	    String s_cui;
	    String s_name;
	    String s_type;
	    String s_novel;
	    String o_cui;
	    String o_name;
	    String o_type;
	    String o_novel;

	    Predication() {
	    }
	}

}

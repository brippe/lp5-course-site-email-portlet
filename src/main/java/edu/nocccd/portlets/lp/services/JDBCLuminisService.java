package edu.nocccd.portlets.lp.services;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.sghe.luminis.community.services.DelegatedCommunityService;
import com.sghe.luminis.core.spring.SpringContextUtility;
import com.sghe.luminis.person.exception.PersonException;
import com.sghe.luminis.person.services.DelegatingPersonService;
import com.sghe.luminis.person.services.PersonService;
import edu.nocccd.portlets.lp.edu.nocccd.portlets.lp.entity.CourseMember;
import edu.nocccd.portlets.lp.edu.nocccd.portlets.lp.entity.Person;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * JDBC service to retrieve Luminis 5 information.
 *
 * @author Brad Rippe 
 * @see edu.nocccd.portlets.lp.services.LuminisService
 */
@Component
public class JDBCLuminisService implements LuminisService {

	private static final Log log = LogFactory.getLog(JDBCLuminisService.class);
	private JdbcTemplate jdbcTemplate;

	public JDBCLuminisService() {
		try {
			Context initContext = new InitialContext();
			Context envContext = (Context) initContext.lookup("java:/comp/env");
			DataSource ds = (DataSource) envContext.lookup("jdbc/LuminisPooledDB");
			this.setDataSource(ds);
		} catch (Exception e) { log.error(e); }
	}

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

	@Override
	public List<CourseMember> getCourseMembership(long groupId) {
		ArrayList<CourseMember> members = new ArrayList<CourseMember>();
		String sql = "select user_id, member_type from lp_community_member where group_id = ? and gt_status = 'A'";
		List<Map<String, Object>> rows = this.jdbcTemplate.queryForList(sql, new Object[] { new Long(groupId) });

		if ((rows != null) && (rows.size() > 0)) {
			for (Map<String, Object> tempRow : rows) {
				CourseMember member = new CourseMember();
				member.setUserId((String)tempRow.get("user_id"));
				member.setMemberType((String)(tempRow.get("member_type")));
				member.setPerson(getPersonByLoginId(member.getUserId().substring(1)));
				member.getPerson().setLoginId(member.getUserId().substring(1));
				members.add(member);
			}
		}
		Collections.sort(members);
		return members;
	}

	@Override
	public String getTermName(String termCode) {
		String sql = "select term_desc from lp_terms where external_term_id = ?";

		return this.jdbcTemplate.queryForObject(sql, new Object[] { termCode }, String.class);
	}

	private Person getPersonByLoginId(String loginId) {
		String sql = "select firstname, lastname, emailaddress from user_ " +
				"left join lp_person on user_.userid = lp_person.portal_user_id " +
				"where login_id = '" + loginId + "' order by lastname, firstname";

		Person p = (Person) this.jdbcTemplate.queryForObject(sql, new Person());
		p.setLoginId(loginId);
		return p;
	}
}
/*
 * JDBCLuminisService.java
 *
 * Copyright (c) Feb 28, 2017 North Orange County Community College District. All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE, ARE EXPRESSLY DISCLAIMED. IN NO EVENT SHALL
 * NOCCCD OR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED, THE COSTS OF PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED IN ADVANCE OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Redistribution and use of this software in source or binary forms, with or
 * without modification, are permitted, provided that the following conditions
 * are met.
 *
 * 1. Any redistribution must include the above copyright notice and disclaimer
 * and this list of conditions in any related documentation and, if feasible, in
 * the redistributed software.
 *
 * 2. Any redistribution must include the acknowledgment, "This product includes
 * software developed by NOCCCD," in any related documentation and, if
 * feasible, in the redistributed software.
 *
 * 3. The names "NOCCCD" and "North Orange County Community College District" must not be used to endorse or
 * promote products derived from this software.
 */

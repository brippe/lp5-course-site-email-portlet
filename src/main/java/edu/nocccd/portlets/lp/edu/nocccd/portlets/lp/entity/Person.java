package edu.nocccd.portlets.lp.edu.nocccd.portlets.lp.entity;


import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Brad Rippe
 */
public class Person implements RowMapper, Comparable<Person> {
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String loginId;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    @Override
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        this.setFirstName(rs.getString(1));
        this.setLastName(rs.getString(2));
        this.setEmailAddress(rs.getString(3));
        return this;
    }

    public int compareTo(Person p) {
        int res = this.lastName.compareTo(p.getLastName());
        if(res != 0) {
            return res;
        } else {
            return this.firstName.compareTo(p.getFirstName());
        }
    }
}

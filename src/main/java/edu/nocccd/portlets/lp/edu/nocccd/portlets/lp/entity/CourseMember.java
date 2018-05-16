package edu.nocccd.portlets.lp.edu.nocccd.portlets.lp.entity;

/**
 * @author Brad Rippe
 */
public class CourseMember implements Comparable<CourseMember> {

    private String userId;
    private String memberType;  // either LE (learner enrolled?) or I for instructor
    private Person person;      // for sites it's L (Leader) or M for member

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {this.userId = userId;}

    public String getMemberType() {
        return memberType;
    }

    public void setMemberType(String memberType) {
        this.memberType = memberType;
    }

    public String getMemberTypeDescription() {
        if(memberType != null) {
            if(memberType.equals("I"))
                return "Instructor";
            else if(memberType.equals("L"))
                return "Leader";
            else if(memberType.equals("LE"))
                return "Student";
            else if(memberType.equals("M"))
                return "Member";
        }
        return "Student";
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public int compareTo(CourseMember c) {
        return this.getPerson().compareTo(c.getPerson());
    }
}

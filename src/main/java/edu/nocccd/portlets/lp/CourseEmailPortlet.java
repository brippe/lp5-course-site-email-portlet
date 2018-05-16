package edu.nocccd.portlets.lp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.portlet.*;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.mail.MailMessage;
import com.liferay.portal.kernel.upload.FileItem;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.theme.ThemeDisplay;

import com.liferay.util.mail.MailEngine;
import com.liferay.util.mail.MailEngineException;
import com.sghe.luminis.community.services.DelegatedCommunityService;
import com.sghe.luminis.community.valueobject.DelegatedCommunityValueObject;
import com.sghe.luminis.person.entity.Person;
import com.sghe.luminis.person.exception.PersonException;
import com.sghe.luminis.person.services.DelegatingPersonService;
import com.sghe.luminis.core.spring.SpringContextUtility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.context.ApplicationContext;

import edu.nocccd.portlets.lp.edu.nocccd.portlets.lp.entity.CourseMember;
import edu.nocccd.portlets.lp.services.JDBCLuminisService;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Course Email Portlet. Allows site members to send email to each other via this portlet.
 *
 * @author Brad Rippe 
 * @version 1.0
 */
@Component
public class CourseEmailPortlet extends MVCPortlet {
	private static final Log log = LogFactoryUtil.getLog(CourseEmailPortlet.class);
    private PortletContext ctx = null;
    private ApplicationContext appCtx;
    private PortletRequestDispatcher dispatcher;
    private DelegatedCommunityService communityService;
    private DelegatingPersonService personService;
    private JDBCLuminisService luminisService;
    private final static int ONE_GB = 1073741824;
    private static final String MEMBER_VIEW = "/jsps/index.jsp";
    private static final String COMPOSE_VIEW = "/jsps/composeEmail.jsp";
    private static final String COMPLETE_VIEW = "/jsps/emailSent.jsp";
    private static final String BAD_EMAILS[] = {"@default.com"};

    /**
     * @see javax.portlet.GenericPortlet#doView(RenderRequest request, RenderResponse response)
     */
    public void doView( RenderRequest request, RenderResponse response ) throws PortletException, IOException {

        String username = getUserName();

        try {
            Person per = personService.findPersonByLoginId(username);
            ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute(WebKeys.THEME_DISPLAY);
            long groupId = themeDisplay.getScopeGroupId();

            PortletSession session = request.getPortletSession();
            DelegatedCommunityValueObject community = communityService.getCommunityById(new Long(groupId));

            StringBuffer title = new StringBuffer();
            if(community != null) {
                title.append(community.getCommunityTitle());

                String communityName = community.getCommunityName(); // crn.term 20666.20162
                String communityType = community.getCommunityType();
                if (communityName != null && communityType != null && communityType.equals("C")) {
                    title.append(" CRN: ").append(community.getCommunityName().substring(0, community.getCommunityName().indexOf('.')));
                    title.append(" Term: ").append(luminisService.getTermName(community.getCommunityName().substring(community.getCommunityName().indexOf('.') + 1)));
                }

                session.setAttribute("title", title, PortletSession.APPLICATION_SCOPE);

                if(communityType != null)
                    session.setAttribute("communityType", communityType, PortletSession.APPLICATION_SCOPE);

                List<CourseMember> members = luminisService.getCourseMembership(groupId);
                session.setAttribute("members", members, PortletSession.APPLICATION_SCOPE);

                // is this the instructor
                boolean isInstructor = false;
                for(CourseMember member : members) {
                    if(member.getPerson() != null && member.getPerson().getLoginId().equals(username)
                            && member.getMemberType() != null && member.getMemberType().equals("I"))
                        isInstructor = true;
                }
                session.setAttribute("isInstructor", new Boolean(isInstructor), PortletSession.APPLICATION_SCOPE);

            } else {
                request.setAttribute("courseError", "We are unable to get information for your course. Please try again later!");
            }

        } catch (Exception e) {
            log.error(e);
        }
        dispatcher = ctx.getRequestDispatcher(MEMBER_VIEW);
        dispatcher.include( request, response );
    }

    /**
     * Compose email message
     * @param actionRequest
     * @param actionResponse
     * @throws IOException
     * @throws PortletException
     */
    public void composeEmail(ActionRequest actionRequest,
                            ActionResponse actionResponse) throws IOException, PortletException {

        PortletSession session = actionRequest.getPortletSession();
        List<CourseMember> members = (List<CourseMember>) session.getAttribute("members", PortletSession.APPLICATION_SCOPE);

        boolean selectAll = false;
        if(actionRequest.getParameter("select_all") != null && actionRequest.getParameter("select_all").equals("checked"))
            selectAll = true;

        //get recipients
        String[] recipients;
        if(selectAll) {
            ArrayList<String> recips = new ArrayList();
            int i = 0;
            for(CourseMember member : members) {
                if(validEmail(member.getPerson().getEmailAddress()))
                    recips.add(member.getPerson().getLoginId());
            }
            recipients = new String[recips.size()];    // get all members as recipients
            recips.toArray(recipients);
        } else {
            recipients = ParamUtil.getParameterValues(actionRequest, "check[]");
        }

        // get names of recipients
        StringBuffer recipientNames = new StringBuffer();
        StringBuffer recipientEmails = new StringBuffer();
        addMembersToRecipientList(members, recipients, recipientNames, recipientEmails);

        session.setAttribute("recipients", recipients, PortletSession.APPLICATION_SCOPE);     // track ids of recipients
        session.setAttribute("recipientNames", recipientNames.toString(), PortletSession.APPLICATION_SCOPE);
        session.setAttribute("recipientEmails", recipientEmails.toString(), PortletSession.APPLICATION_SCOPE);
        actionResponse.setRenderParameter("mvcPath", COMPOSE_VIEW);
    }

    /**
     * Send email
     * @param actionRequest
     * @param actionResponse
     * @throws IOException
     * @throws PortletException
     */
    public void sendEmail(ActionRequest actionRequest,
                             ActionResponse actionResponse) throws IOException, PortletException {

        String username = getUserName();

        String subject = actionRequest.getParameter("subject");
        String msg = actionRequest.getParameter("editor");          // this is the message

        PortletSession session = actionRequest.getPortletSession();
        // comma separated email list
        String title = (String) session.getAttribute("title");
        String emailsStr = (String) session.getAttribute("recipientEmails", PortletSession.APPLICATION_SCOPE);
        actionRequest.setAttribute("msgSubject", subject);

        try {
            Person per = personService.findPersonByLoginId(username);

            List<String> emails = Arrays.asList(emailsStr.split("\\s*,\\s*"));
            ArrayList<InternetAddress> emailAddresses = new ArrayList<InternetAddress>();

            for(String email : emails) {
                // if we have a bad email address, skip it
                try {emailAddresses.add(new InternetAddress(email));}
                catch(AddressException ae) {log.error("Bad email address " + email);}
            }

            String server = actionRequest.getServerName();
            boolean isTestServer = false;
            if(server != null && (server.equals("mgtest.nocccd.edu") || server.equals("admintest.nocccd.edu"))) {
                isTestServer = true;
                actionRequest.setAttribute("testMsg", "You are using this portlet on a test instance and it WILL NOT send emails to users on a TEST deployment.");
            }
            // ./products/tomcat/tomcat-portal/lib/ext/mail.jar
            InternetAddress from = new InternetAddress(per.getEmailAddress()); // from the current user

            InternetAddress to = new InternetAddress("brippe@nocccd.edu"); // should be the user
            MailMessage mail;
            if(isTestServer) {
                mail = new MailMessage(from, to, subject, msg, true);
            } else {
                mail = new MailMessage(from, from, subject, msg, true);
                InternetAddress[] bccs = emailAddresses.toArray(new InternetAddress[emailAddresses.size()]);
                mail.setBCC(bccs);
            }

            ArrayList<File> attachments = (ArrayList<File>) session.getAttribute("attachments", PortletSession.APPLICATION_SCOPE);
            if(attachments != null) {
                for (File file : attachments)
                    mail.addFileAttachment(file);
            }

            MailEngine.send(mail);

            if(attachments != null) {
                // clean up attachment files
                for (File file : attachments)
                    file.delete();
            }

        } catch (AddressException e) {
            log.error("Error sending email from site " + title + " From: " + username + " To: " + emailsStr, e);
        } catch (MailEngineException me) {
            log.error("Error sending email from site " + title + " From: " + username + " To: " + emailsStr, me);
        } catch(PersonException pe) {
            log.error("Error sending email from site " + title + " From: " + username + " To: " + emailsStr, pe);
        }

        actionResponse.setRenderParameter("mvcPath", COMPLETE_VIEW);
    }

    /**
     * Handles file uploads
     * @param request
     * @param response
     * @throws PortletException
     * @throws IOException
     * @throws SystemException
     * @throws PortalException
     * @throws Exception
     */
    public void uploadFiles(ActionRequest request, ActionResponse response) throws Exception {

        PortletSession session = request.getPortletSession();
        UploadPortletRequest uploadRequest = PortalUtil.getUploadPortletRequest(request);
        Map<String, FileItem[]> files = uploadRequest.getMultipartParameterMap();

        if (files.size() > 0) {
            FileItem[] file = files.get("file");

            String fileName = file[0].getFileName();
            File tempFile = uploadRequest.getFile("file");
            String abPath = tempFile.getAbsolutePath();
            String tempFileName = tempFile.getName();
            String dirPath = abPath.replace(tempFileName, "");

            File folder = new File(dirPath);
            File filePath = new File(folder.getAbsolutePath() + File.separator + fileName);
            Files.copy(Paths.get(abPath), Paths.get(folder.getAbsolutePath() + File.separator + fileName), REPLACE_EXISTING);

            ArrayList<File> attachments = (ArrayList<File>) session.getAttribute("attachments", PortletSession.APPLICATION_SCOPE);
            if(attachments == null) {
                attachments = new ArrayList<File>();
            }
            // only allow five attachments
            if(attachments.size() < 5)
                attachments.add(filePath);
            session.setAttribute("attachments", attachments, PortletSession.APPLICATION_SCOPE);
        }
    }

    /**
     * @see javax.portlet.GenericPortlet#init(PortletConfig config)
     */
    public void init( PortletConfig config ) throws PortletException {
        super.init( config );
        ctx = config.getPortletContext();

        communityService = (DelegatedCommunityService) SpringContextUtility.getBean("delegatedCommunitySvc");
        personService = (DelegatingPersonService) SpringContextUtility.getBean("personService");
        luminisService = (JDBCLuminisService) SpringContextUtility.getBean("nocccdLuminisService");
    }

    /**
     * @see javax.portlet.GenericPortlet#destroy()
     */
    public void destroy() {
        dispatcher = null;
        super.destroy();
    }

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        appCtx = applicationContext;
    }

    private void addMembersToRecipientList(List<CourseMember> members, String[] recipients, StringBuffer recipientNames, StringBuffer recipientEmails) {
        for(CourseMember member : members) {
            for(int i = 0; i < recipients.length; i++) {
                if (member.getPerson().getLoginId().equals(recipients[i])) {
                    recipientNames.append(member.getPerson().getFirstName()).append(" ")
                            .append(member.getPerson().getLastName()).append(", ");
                    recipientEmails.append(member.getPerson().getEmailAddress()).append(",");
                }
            }
        }
        if(recipientNames.lastIndexOf(",") > 0)
            recipientNames.deleteCharAt(recipientNames.lastIndexOf(","));
        if(recipientEmails.lastIndexOf(",") > 0)
            recipientEmails.deleteCharAt(recipientEmails.lastIndexOf(","));
    }

    private String getUserName() {
        SecurityContext context = SecurityContextHolder.getContext();

        String username = null;
        if ( null != context ) {
            Authentication auth = context.getAuthentication();
            if ( null != auth ) {
                username = auth.getName();
            }
        }
        return username;
    }

    private boolean validEmail(String emailAddress) {
        if(emailAddress == null)
            return false;

        for(int i = 0; i < BAD_EMAILS.length; i++) {
            if(emailAddress.contains(BAD_EMAILS[i]))
                return false;
        }
        return true;
    }
}
/*
 * CourseEmailPortlet.java
 *
 * Copyright (c) February 27, 2017 North Orange County Community College District. All rights reserved.
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

<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib  uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<%@ taglib uri="http://alloy.liferay.com/tld/aui" prefix="aui" %>

<%--Defines a number of theme objects, etc --%>
<liferay-theme:defineObjects />
<link href="${pageContext.request.contextPath}/css/uploadfile.css" rel="stylesheet">
<script src="${pageContext.request.contextPath}/js/jquery.uploadfile.min.js"></script>
<portlet:defineObjects />
<portlet:actionURL var="sendEmailActionURL" windowState="normal" name="sendEmail"></portlet:actionURL>
<portlet:renderURL var="recipientsURL" windowState="normal"></portlet:renderURL>
<div class="container">
    <div class="row">
        <c:choose>
            <c:when test="${empty courseError}">
                <h3><c:out value="${sessionScope.title}" /></h3>
            </c:when>
            <c:otherwise>
                <c:out value="${courseError}"/>
            </c:otherwise>
        </c:choose>
    </div>

    <aui:form action="<%=sendEmailActionURL%>" name="composeEmailForm" method="post">
        <div class="row">
            <a class="btn btn-primary" role="button" href="<%=recipientsURL%>" style="text-align: right">Back to Members</a>
            <a class="btn btn-primary" role="button" data-toggle="collapse" href="#collapseRecipients" aria-expanded="false" aria-controls="collapseRecipients" style="text-align: left">View Recipients</a> (<c:out value="${fn:length(sessionScope.recipients)}"/> recipients)<br/><br/>
            <div class="collapse" id="collapseRecipients">
                <div class="well">
                    <c:out value="${sessionScope.recipientNames}"/>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="form-group">
                <aui:input id="${renderResponse.getNamespace()}subject" name="${renderResponse.getNamespace()}subject" label="Subject" placeholder="Enter message subject" autoFocus="true" required="true">
                </aui:input>
            </div>
            <div class="form-group">
                <label for="input-editor">Message</label>
                <liferay-ui:input-editor />
                <input name="<portlet:namespace />htmlCodeFromEditorPlacedHere" type="hidden" value="" />
            </div>
        </div>
        <div class="row">
            <portlet:actionURL var="uploadFileURL" name="uploadFiles"/>
            <script>
                $(document).ready(function(){$("#multipleupload").uploadFile({url:"<%= uploadFileURL.toString() %>", multiple:true, dragDrop:true, sequential:true, sequentialCount:3});});
            </script>
            <div id="multipleupload">Upload</div>
        </div>
        <div class="row">
            <input type="submit" id="sendEmail" name="sendEmail" value="Send Email" /> &nbsp;<span>NOTE: Yahoo regularly blocks mass emails. This message may NOT be received by addresses @ yahoo.com!"</span>
        </div>
    </aui:form>
</div>

<script>
    function <portlet:namespace />initEditor() {return '<em>Type your message text here!</em>';}
    function <portlet:namespace />extractCodeFromEditor() {document.<portlet:namespace />fm.<portlet:namespace />htmlCodeFromEditorPlacedHere.value = window.<portlet:namespace />editor.getHTML();}
</script>


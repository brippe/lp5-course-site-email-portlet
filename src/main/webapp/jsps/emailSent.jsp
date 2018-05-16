<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<%--Defines a number of theme objects, etc --%>
<liferay-theme:defineObjects />
<%--Defines renderRequest, renderResponse, etc --%>
<portlet:defineObjects />
<portlet:renderURL  var="portletRenderURL" />
<div class="container">
    <div class="row">
        <c:choose>
            <c:when test="${empty courseError}">
                <h3><c:out value="${sessionScope.title}" /></h3><br/>
                <div>Your email, <strong>'<c:out value="${msgSubject}"/>'</strong>, has been scheduled for delivery!<br/><br/></div>
                <c:if test="${testMsg != null}"><div><em><c:out value="${testMsg}"/></em><br/><br/></div></c:if>
                <div><a class="btn btn-primary" role="button" href="<%=portletRenderURL%>" style="text-align: right">Back to Members</a></div>
            </c:when>
            <c:otherwise>
                <c:out value="${courseError}"/>
            </c:otherwise>
        </c:choose>
        <c:remove var="title" scope="session" />
        <c:remove var="communityType" scope="session" />
        <c:remove var="members" scope="session" />
    </div>
</div>

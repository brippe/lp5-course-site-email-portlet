<%@ page import="java.util.ArrayList" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>
<%@ taglib prefix="aui" uri="http://alloy.liferay.com/tld/aui" %>

<%--Defines a number of theme objects, etc --%>
<liferay-theme:defineObjects />
<%--Defines renderRequest, renderResponse, etc--%>
<portlet:defineObjects />
<portlet:actionURL var="composeEmailActionURL" windowState="normal" name="composeEmail"></portlet:actionURL>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/jquery.dataTables.min.css" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/select.dataTables.min.css" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/buttons.dataTables.min.css" />
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.dataTables.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables.select.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables.buttons.min.js"></script>
<script type="text/javascript" src="//cdnjs.cloudflare.com/ajax/libs/jszip/2.5.0/jszip.min.js"></script>
<script type="text/javascript" src="//cdn.rawgit.com/bpampuch/pdfmake/0.1.24/build/pdfmake.min.js"></script>
<script type="text/javascript" src="//cdn.rawgit.com/bpampuch/pdfmake/0.1.24/build/vfs_fonts.js"></script>
<script type="text/javascript" src="//cdn.datatables.net/buttons/1.2.4/js/buttons.html5.min.js"></script>
<div class="container">
    <div class="row">
        <c:choose>
            <c:when test="${empty courseError}">
                <h3><c:out value="${sessionScope.title}" /></h3>
                Hi <c:out value="${user.getFullName()}" />! Here's your membership information:<br/><br/>
            </c:when>
            <c:otherwise>
                <c:out value="${courseError}"/>
            </c:otherwise>
        </c:choose>
        <div class = "alert alert-danger" id="norecipients">
            <strong>Warning!</strong> Please select at least one recipient by clicking on a row!
        </div>
    </div>
    <% ArrayList<Integer> invalidIndices = new ArrayList<Integer>(); %>
    <aui:form action="<%=composeEmailActionURL%>" id="courseMemberForm" name="courseMemberForm" method="post">
        <div class="row">
            <table class="table table-hover table-bordered" cellspacing="0" width="100%" id="coursemembers">
                <thead><tr>
                    <th><input type="hidden" id="${renderResponse.getNamespace()}select_all" name="${renderResponse.getNamespace()}select_all" value=""/></th>
                    <th>Name</th>
                    <c:if test="${sessionScope.communityType eq 'C' && sessionScope.isInstructor}"><th>Email</th><th>User Id</th></c:if>
                    <th>Membership</th>
                </tr></thead>
                <% int index = 0; %>
                <c:forEach items="${sessionScope.members}" var="i" >
                    <tr>
                        <td>
                            <c:set var="emailAddr" value="${i.getPerson().getEmailAddress()}"/>
                            <c:set var="validEmail" value="${not empty emailAddr and not fn:containsIgnoreCase(emailAddr, 'default.com')}" />
                            <c:if test="${validEmail}">
                                <input class="checkbox" type="checkbox" id="${renderResponse.getNamespace()}check[]" name="${renderResponse.getNamespace()}check[]" value="${i.getPerson().getLoginId()}"/>
                                <% invalidIndices.add(index); %>
                            </c:if>
                        </td>
                        <td><c:out value="${i.getPerson().getLastName()}" />, <c:out value="${i.getPerson().getFirstName()}"/></td>
                        <c:if test="${sessionScope.communityType eq 'C' && sessionScope.isInstructor}">
                        <td><c:choose>
                            <c:when test="${validEmail}">
                                <c:out value="${emailAddr}"/>
                            </c:when>
                            <c:otherwise><span style="color:indianred;font-style:italic">No Email (user needs to update their email in Banner)</span></c:otherwise>
                            </c:choose>
                        </td><td><c:out value="${i.getPerson().getLoginId()}"/></td>
                        </c:if>
                        <td><c:out value="${i.getMemberTypeDescription()}"/></td>
                    </tr>
                    <% index++; %>
                </c:forEach>
            </table>
        </div>
        <div class="row"><input type="submit" id="composeEmail" name="composeEmail" value="Compose Email" /></div>
    </aui:form>
</div>
<script>
    $("#composeEmail").prop('disabled', true);
    $(document).ready(function() {
        var table = $('#coursemembers').DataTable({
                dom: 'Bfrtip', select: {style: 'multi'},
                saveState: true,
                buttons: [{
                    extend: 'excelHtml5',
                    title: '<c:out value="${sessionScope.title}" /> CRN: <c:out value="${sessionScope.crn}" /> Term Code: <c:out value="${sessionScope.termCode}" />',
                    filename: '<c:out value="${sessionScope.title}" />',
                    exportOptions: {columns: ':visible'}
                },
                {
                    extend: 'pdfHtml5',
                    title: '<c:out value="${sessionScope.title}" /> CRN: <c:out value="${sessionScope.crn}" /> Term Code: <c:out value="${sessionScope.termCode}" />',
                    filename: '<c:out value="${sessionScope.title}" />',
                    exportOptions: {columns: [1, 2, 3]}
                },
                {
                    text: 'All Members', action: function () {
                    $(".checkbox").prop('checked', true);
                    $("#<portlet:namespace/>select_all").val('checked');
                    $('#norecipients').hide();
                    $("#composeEmail").prop('disabled', false);
                    // check all rows even ones not currently displayed
                    var rows = table.rows().nodes();
                    $('input[type="checkbox"]', rows).prop('checked', true);
                    table.rows().select();
                    for(var i = 0; i < table.rows().count(); i++) {
                        //alert(table.rows(i).data()[0][0]);
                        if(table.rows(i).data()[0][0] === '')
                            table.row(i).deselect();
                    }
                }
                },
                {
                    text: 'Clear All', action: function () {
                    table.rows().deselect();
                    $(".checkbox").prop('checked', false);
                    $("#<portlet:namespace/>select_all").val('');
                    $('#norecipients').show();
                    $("#composeEmail").prop('disabled', true);
                    // check all rows even ones not currently displayed
                    var rows = table.rows().nodes();
                    $('input[type="checkbox"]', rows).prop('checked', false);
                }
            }]
        });
        table.order( [ 1, 'asc' ] ).draw();

        // initial size of members
        var initMembers = $('.checkbox').length;
        $('#coursemembers tbody').on('click', 'tr', function () {
            if ($(this).hasClass('selected')) {
                $("#composeEmail").prop('disabled', false);
                $(this).find("input[type=checkbox]").prop('checked', true);

            } else {
                $(this).find("input[type=checkbox]").prop('checked', false);
                $("#<portlet:namespace/>select_all").val('');
            }
            if (table.rows({selected: true}).count() == 0) {
                $("#composeEmail").prop('disabled', true);
                $('#norecipients').show();
            } else {
                $('#norecipients').hide();
            }
        });

        $('.checkbox').change(function () {
            if (!$(this).prop("checked")) {
                $("#<portlet:namespace/>select_all").val('');
            }
            if ($('.checkbox:checked').length == initMembers) {
                $("#<portlet:namespace/>select_all").val('checked');
            }
        });

        $('#${renderResponse.getNamespace()}courseMemberForm').on('submit', function(e) {
            var form = this;
            var data = table.rows({selected:  true}).data();
            //clear checkboxes then add them again
            $('input:checkbox').removeAttr('checked');

            for(var i=0; i<data.length;i++) {
                if(data[i][0] != '')
                    $(form).append(data[i][0].slice(0, -1) + ' style="display:none;" checked>');
            }
        });

        table.on('user-select', function (e, dt, type, cell, originalEvent) {   // called when a row is selected
            var rows = dt.data();
            // if there's no checkbox, don't allow selection
            if (rows[cell.index().row][0] === '') {
                e.preventDefault();
            }
        });
    });
</script>

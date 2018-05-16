package edu.nocccd.portlets.lp.services;

import java.util.List;
import edu.nocccd.portlets.lp.edu.nocccd.portlets.lp.entity.CourseMember;

/**
 * A service interface for user information from Luminis 5.
 * @author Brad Rippe 
 */
public interface LuminisService {


    List<CourseMember> getCourseMembership(long groupId);

    String getTermName(String termCode);

}

/*
 * LuminisService.java
 *
 * Copyright (c) Mar 1, 2017 North Orange County Community College District. All rights reserved.
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

/* (c) JLPT E-Learning Platform */
package com.jlpt.dto.response;

import java.util.List;

public record DashboardResponse(int streak, List<Boolean> weekDays, long wordCount, long daysThisMonth) {}

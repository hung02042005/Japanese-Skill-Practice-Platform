/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.auth.event;

public record SendPasswordResetEmailEvent(String email, String token) {}

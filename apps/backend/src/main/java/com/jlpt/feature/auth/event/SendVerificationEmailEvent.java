/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.auth.event;

public record SendVerificationEmailEvent(String email, String token) {}

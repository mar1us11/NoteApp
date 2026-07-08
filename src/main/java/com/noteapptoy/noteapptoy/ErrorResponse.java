package com.noteapptoy.noteapptoy;

public record ErrorResponse(

        int status,

        String error,

        String message

) {}
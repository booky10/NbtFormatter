package dev.booky.nbtfmt.routes;
// Created by booky10 in NbtFormatter (01:42 06.04.23)

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.TagStringIO;

import java.io.IOException;

public final class ApiRoutes {

    private static final String PREFIX = "api/v1/";

    private ApiRoutes() {
    }

    public static void register(Javalin javalin) {
        javalin.post(PREFIX + "format", ApiRoutes::handleFormatReq);
    }

    private static void handleFormatReq(Context ctx) {
        String indentStr = ctx.queryParam("indent");
        int indent = indentStr == null ? 2 : Integer.parseInt(indentStr);

        long totalStart = System.nanoTime();
        try {
            TagStringIO processor = TagStringIO.builder().indent(indent).build();

            long parsingStart = System.nanoTime();
            CompoundBinaryTag parsed = processor.asCompound(ctx.body());
            long parsingTime = System.nanoTime() - parsingStart;
            ctx.header("Parsing-Time", Long.toString(parsingTime));

            long formatStart = System.nanoTime();
            String formatted = processor.asString(parsed);
            long formatTime = System.nanoTime() - formatStart;
            ctx.header("Format-Time", Long.toString(formatTime));

            ctx.result(formatted);
            ctx.status(HttpStatus.OK);
        } catch (IOException exception) {
            Throwable cause = exception.getCause();
            if (cause == null || !"StringTagParseException".equals(cause.getClass().getSimpleName())) {
                throw new RuntimeException(exception);
            }

            ctx.result(exception.getMessage());
            ctx.status(HttpStatus.BAD_REQUEST);
        } finally {
            long totalTime = System.nanoTime() - totalStart;
            ctx.header("Total-Time", Long.toString(totalTime));
        }
    }
}

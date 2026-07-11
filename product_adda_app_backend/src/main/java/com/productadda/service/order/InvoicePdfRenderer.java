package com.productadda.service.order;

import java.io.ByteArrayOutputStream;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import com.productadda.exception.ApiException;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * INVOICE PDF RENDERER
 * Description: Thin utility component rendering the "invoice-template"
 * Thymeleaf template into a PDF byte array via openhtmltopdf. Not a
 * business service (no 5-section skeleton) -- technical infrastructure
 * helper, same category as UuidUtil/HashUtil.
 *
 * REQUIRES the template file at:
 * src/main/resources/templates/invoice-template.html
 * (included separately in this delivery under resources/templates/).
 * ================================================================
 */
@Component
@RequiredArgsConstructor
public class InvoicePdfRenderer {

    private final SpringTemplateEngine templateEngine;

    public byte[] renderInvoicePdf(Context templateContext) {

        if (templateContext == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "templateContext is required to render invoice PDF");
        }

        String renderedHtml = templateEngine.process("invoice-template", templateContext);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(renderedHtml, null);
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();

        } catch (Exception ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to render invoice PDF: " + ex.getMessage());
        }
    }
}

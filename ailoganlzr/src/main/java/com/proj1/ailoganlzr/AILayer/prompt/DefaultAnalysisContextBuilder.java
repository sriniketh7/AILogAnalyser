package com.proj1.ailoganlzr.AILayer.prompt;

import com.proj1.ailoganlzr.DTO.CodeSnippet;
import com.proj1.ailoganlzr.service.Interface.AnalysisContextBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultAnalysisContextBuilder implements AnalysisContextBuilder {

    @Override
    public String build(String rawStackTrace,
                        List<CodeSnippet> snippets,
                        List<String> knowledgeDocuments) {

        StringBuilder context = new StringBuilder();

        /*
         * ===========================================================
         * CASE SUMMARY
         * ===========================================================
         */

        context.append("==================================================\n");
        context.append("CASE SUMMARY\n");
        context.append("==================================================\n\n");

        if (!snippets.isEmpty()) {

            CodeSnippet primary = snippets.get(0);

            context.append("Primary Class : ")
                    .append(primary.getFullyQualifiedClassName())
                    .append("\n");

            context.append("Primary Method : ")
                    .append(primary.getMethodName())
                    .append("\n");

            context.append("Line Range : ")
                    .append(primary.getStartLine())
                    .append(" - ")
                    .append(primary.getEndLine())
                    .append("\n");

        } else {

            context.append("No source code could be extracted.\n");

        }

        context.append("\n");

        /*
         * ===========================================================
         * RELEVANT SOURCE CODE
         * ===========================================================
         */

        context.append("==================================================\n");
        context.append("RELEVANT SOURCE CODE\n");
        context.append("==================================================\n\n");

        if (snippets.isEmpty()) {

            context.append("No relevant source code available.\n\n");

        } else {

            int index = 1;

            for (CodeSnippet snippet : snippets) {

                context.append("Snippet ")
                        .append(index++)
                        .append("\n");

                context.append("----------------------------------------\n");

                context.append("Class : ")
                        .append(snippet.getFullyQualifiedClassName())
                        .append("\n");

                context.append("Method : ")
                        .append(snippet.getMethodName())
                        .append("\n");

                context.append("Lines : ")
                        .append(snippet.getStartLine())
                        .append(" - ")
                        .append(snippet.getEndLine())
                        .append("\n\n");

                context.append(snippet.getSourceCode())
                        .append("\n\n");
            }
        }

        /*
         * ===========================================================
         * KNOWLEDGE BASE
         * ===========================================================
         */

        context.append("==================================================\n");
        context.append("RETRIEVED KNOWLEDGE BASE\n");
        context.append("==================================================\n\n");

        if (knowledgeDocuments == null || knowledgeDocuments.isEmpty()) {

            context.append("No relevant knowledge documents found.\n\n");

        } else {

            int index = 1;

            for (String document : knowledgeDocuments) {

                context.append("Document ")
                        .append(index++)
                        .append("\n");

                context.append("----------------------------------------\n");

                context.append(document)
                        .append("\n\n");
            }
        }

        /*
         * ===========================================================
         * RAW STACK TRACE
         * ===========================================================
         */

        context.append("==================================================\n");
        context.append("RAW STACK TRACE\n");
        context.append("==================================================\n\n");

        context.append(rawStackTrace);

        return context.toString();
    }
}

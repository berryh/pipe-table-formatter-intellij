package net.berryh.pipetableformatter.formatter;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.intellij.lang.Language;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PipeTablePostFormatProcessor implements PostFormatProcessor
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	// TODO This should be configurable.
	private final List<Language> languagesToFormat = new ArrayList<>();
	private final PipeTableFormatter formatter;

	public PipeTablePostFormatProcessor()
	{
		LOGGER.info("Initializing PipeTablePostFormatProcessor");
		this.formatter = new PipeTableFormatter();
		final Language story = Language.findLanguageByID("Story");
		LOGGER.info("Found Story language: '{}'", story);
		if (story != null)
		{
			languagesToFormat.add(story);
		}
		LOGGER.info("Initialized PipeTablePostFormatProcessor");
	}

	@Override
	@Nonnull
	public PsiElement processElement(@NotNull final PsiElement source, @NotNull final CodeStyleSettings settings)
	{
		// Haven't figured out yet what this does, so just return the original source.
		// We tried throwing an exception, but this does not provide any more info.
		// Seems to be some sort of CodeInsight-related method.
		final Throwable throwable = new Throwable();
		throwable.fillInStackTrace();
		LOGGER.info("ProcessElement has been called!", throwable);
		return source;
	}

	@Override
	@Nonnull
	public TextRange processText(@NotNull final PsiFile sourceFile, @NotNull final TextRange inputRange, @NotNull final CodeStyleSettings settings)
	{
		LOGGER.info("ProcessText has been called.");
		LOGGER.info("Language for this file is: {}", sourceFile.getLanguage());
		if (!languagesToFormat.contains(sourceFile.getLanguage()))
		{
			return inputRange;
		}

		LOGGER.info("Getting project and document.");
		final Project project = sourceFile.getProject();
		final Document document = PsiDocumentManager.getInstance(project).getDocument(sourceFile);
		if (document == null)
		{
			LOGGER.info("Document is null.");
			return inputRange;
		}
		if (!ReadonlyStatusHandler.ensureDocumentWritable(project, document))
		{
			LOGGER.info("Document is not writable.");
			return inputRange;
		}

		LOGGER.info("Document exists and is writable.");
		final String sourceToFormat = inputRange.substring(sourceFile.getText());
		final String formattedSource = formatter.format(sourceToFormat);
		final String finalContent = StringUtil.convertLineSeparators(formattedSource);
		LOGGER.info("Document content has been formatted.");

		WriteCommandAction.runWriteCommandAction(project, "Format Pipe Table", "Pipe Table Formatter", () -> {
			LOGGER.info("Writing document.");
			document.replaceString(inputRange.getStartOffset(), inputRange.getEndOffset(), finalContent);
			PsiDocumentManager.getInstance(project).commitDocument(document);
			LOGGER.info("Document has been written and is committed.");
		}, sourceFile);

		LOGGER.info("Done processing, returning a new text range.");
		return TextRange.from(inputRange.getStartOffset(), finalContent.length());
	}
}

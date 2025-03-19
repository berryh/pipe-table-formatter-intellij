package net.berryh.pipetableformatter.formatter;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

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
		LOGGER.debug("Initializing PipeTablePostFormatProcessor");
		this.formatter = new PipeTableFormatter();
		final Language story = Language.findLanguageByID("Story");
		LOGGER.debug("Found Story language: '{}'", story);
		if (story != null)
		{
			languagesToFormat.add(story);
		}
		LOGGER.debug("Initialized PipeTablePostFormatProcessor");
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
		LOGGER.debug("ProcessElement has been called!", throwable);

		processText(source.getContainingFile(), source.getTextRange(), settings);
		return source;
	}

	@Override
	@Nonnull
	public TextRange processText(@NotNull final PsiFile sourceFile, @NotNull final TextRange inputRange, @NotNull final CodeStyleSettings settings)
	{
		LOGGER.debug("ProcessText has been called.");
		LOGGER.debug("Language for this file is: {}", sourceFile.getLanguage());
		if (!languagesToFormat.contains(sourceFile.getLanguage()))
		{
			return inputRange;
		}

		LOGGER.debug("Getting project and document.");
		final Project project = sourceFile.getProject();
		final Document document = PsiDocumentManager.getInstance(project).getDocument(sourceFile);
		if (document == null)
		{
			LOGGER.debug("Document is null.");
			return inputRange;
		}
		if (!ReadonlyStatusHandler.ensureDocumentWritable(project, document))
		{
			LOGGER.debug("Document is not writable.");
			return inputRange;
		}

		LOGGER.debug("Document exists and is writable.");
		final String sourceToFormat = inputRange.substring(sourceFile.getText());
		final String formattedSource = formatter.format(sourceToFormat);
		final String finalContent = StringUtil.convertLineSeparators(formattedSource);
		LOGGER.debug("Document content has been formatted.");

		WriteCommandAction.runWriteCommandAction(project, "Format Pipe Table", "Pipe Table Formatter", () -> {
			LOGGER.debug("Writing document.");
			document.replaceString(inputRange.getStartOffset(), inputRange.getEndOffset(), finalContent);
			PsiDocumentManager.getInstance(project).commitDocument(document);
			LOGGER.debug("Document has been written and is committed.");
		}, sourceFile);

		LOGGER.debug("Done processing, returning a new text range.");
		return TextRange.from(inputRange.getStartOffset(), finalContent.length());
	}
}

package net.berryh.pipetableformatter.action;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import net.berryh.pipetableformatter.exception.PipeTableFormatterException;
import org.jurr.pipetableformatter.TableFormatter;

public class FormatAllAction extends EditorAction
{
	protected FormatAllAction()
	{
		super(new EditorActionHandler()
		{
			@Override
			protected void doExecute(@Nonnull final Editor editor, @Nullable final Caret caret, @Nonnull final DataContext dataContext)
			{
				ApplicationManager.getApplication().runWriteAction(() -> {
					final Document document = editor.getDocument();
					final String documentText = document.getText();
					final int assumedNewContentSize = (int) (documentText.getBytes().length * 1.1d);

					try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(assumedNewContentSize);
					     final PrintStream printStream = new PrintStream(outputStream, false, StandardCharsets.UTF_8.name());
					     final ByteArrayInputStream inputStream = new ByteArrayInputStream(documentText.getBytes());
					     final InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8))
					{
						final TableFormatter tableFormatter = new TableFormatter(printStream);
						tableFormatter.format(inputStreamReader);
						tableFormatter.close();

						final String newContent = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
						// IntelliJ document content can only have '\n' line separators.
						final String sanitizedContent = newContent.replaceAll(System.lineSeparator(), "\n");
						document.setText(sanitizedContent);
					}
					catch (IOException e)
					{
						throw new PipeTableFormatterException(e);
					}
				});
			}
		});
	}
}
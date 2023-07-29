package net.berryh.pipetableformatter.action;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import org.jurr.pipetableformatter.PipeTableFormatter;

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
					final String formattedDocument = new PipeTableFormatter().pipeTablesInString(documentText);
					document.setText(formattedDocument);
				});
			}
		});
	}
}

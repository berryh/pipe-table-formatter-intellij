package net.berryh.pipetableformatter.exception;

import javax.annotation.Nonnull;

public class PipeTableFormatterException extends RuntimeException
{
	public PipeTableFormatterException(@Nonnull final String message, @Nonnull final Throwable cause)
	{
		super(message, cause);
	}

	public PipeTableFormatterException(@Nonnull final Throwable cause)
	{
		super(cause);
	}
}

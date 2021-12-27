package com.foo

private[foo] final case class ReportableError(msg: String) extends RuntimeException(msg)
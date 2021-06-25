/*
 * Copyright (c) 2021.  Enzo Reyes Licensed under the Apache License, Version 2.0 (the "License");   you may
 * not use this file except in compliance with the License.   You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and limitations under the License.
 *
 */

package fettuccineshell;

import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Value;

public class FettuccineShellHelper {

  @Value("${shell.out.info}")
  public String infoColor;

  @Value("${shell.out.success}")
  public String successColor;

  @Value("${shell.out.warning}")
  public String warningColor;

  @Value("${shell.out.error}")
  public String errorColor;

  private Terminal terminal;

  public FettuccineShellHelper(Terminal terminal) {
    this.terminal = terminal;
  }

  public String getColored(String message, PromptColor color) {
    return (new AttributedStringBuilder())
        .append(message, AttributedStyle.DEFAULT.foreground(color.toJlineAttributedStyle()))
        .toAnsi();
  }

  public String getInfoMessage(String message) {
    return getColored(message, PromptColor.valueOf(infoColor));
  }

  public String getSuccessMessage(String message) {
    return getColored(message, PromptColor.valueOf(successColor));
  }

  public String getWarningMessage(String message) {
    return getColored(message, PromptColor.valueOf(warningColor));
  }

  public String getErrorMessage(String message) {
    return getColored(message, PromptColor.valueOf(errorColor));
  }

  /**
   * Print message to the console in the default color.
   *
   * @param message message to print
   */
  public void print(String message) {
    print(message, null);
  }

  /**
   * Print message to the console in the success color.
   *
   * @param message message to print
   */
  public void printSuccess(String message) {
    print(message, PromptColor.valueOf(successColor));
  }

  /**
   * Print message to the console in the info color.
   *
   * @param message message to print
   */
  public void printInfo(String message) {
    print(message, PromptColor.valueOf(infoColor));
  }

  /**
   * Print message to the console in the warning color.
   *
   * @param message message to print
   */
  public void printWarning(String message) {
    print(message, PromptColor.valueOf(warningColor));
  }

  /**
   * Print message to the console in the error color.
   *
   * @param message message to print
   */
  public void printError(String message) {
    print(message, PromptColor.valueOf(errorColor));
  }

  /**
   * Generic Print to the console method.
   *
   * @param message message to print
   * @param color   (optional) prompt color
   */
  public void print(String message, PromptColor color) {
    String toPrint = message;
    if (color != null) {
      toPrint = getColored(message, color);
    }
    terminal.writer().println(toPrint);
    terminal.flush();
  }

  // --- set / get methods ---------------------------------------------------

  public Terminal getTerminal() {
    return terminal;
  }

  public void setTerminal(Terminal terminal) {
    this.terminal = terminal;
  }
}

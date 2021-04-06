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

import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Parser;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.jline.JLineShellAutoConfiguration;

@Configuration
public class ShellConfig {

  @Bean
  public FettuccineShellHelper shellHelper(@Lazy Terminal terminal) {
    return new FettuccineShellHelper(terminal);
  }

  @Bean
  public InputReader inputReader(
      @Lazy Terminal terminal,
      @Lazy Parser parser,
      JLineShellAutoConfiguration.CompleterAdapter completer,
      @Lazy History history,
      FettuccineShellHelper shellHelper) {
    LineReaderBuilder lineReaderBuilder =
        LineReaderBuilder.builder()
            .terminal(terminal)
            .completer(completer)
            .history(history)
            .highlighter(
                (LineReader reader, String buffer) -> {
                  return new AttributedString(
                      buffer,
                      AttributedStyle.BOLD.foreground(PromptColor.WHITE.toJlineAttributedStyle()));
                })
            .parser(parser);

    LineReader lineReader = lineReaderBuilder.build();
    lineReader.unsetOpt(LineReader.Option.INSERT_TAB);
    return new InputReader(lineReader, shellHelper);
  }
}

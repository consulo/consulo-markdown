package consulo.markdown;

import consulo.annotation.component.ExtensionImpl;
import consulo.fileEditor.ReadMeFileProvider;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author VISTALL
 * @since 2025-01-22
 */
@ExtensionImpl(id = "markdown")
public class MarkdownReadMeFileProvider implements ReadMeFileProvider {
  private List<String> myPaths = List.of("README.md", "docs/README.md");

  @Nullable
  @Override
  public Path resolveFile(@Nonnull Path path) {
    for (String child : myPaths) {
      Path resolved = path.resolve(child);
      if (Files.exists(resolved)) {
        return resolved;
      }
    }
    return null;
  }
}

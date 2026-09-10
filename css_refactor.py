import re

css_lines = open('apps/web/src/app/app.component.css').readlines()

# The global top bar is around line 1850.
# /* --- Reader Platform & Global UI --- */ is around 1849.
reader_ui_start = next(i for i, line in enumerate(css_lines) if '/* --- Reader Platform & Global UI --- */' in line)

studio_css = "".join(css_lines[:reader_ui_start])

reader_platform_start = next(i for i, line in enumerate(css_lines) if '/* Reader Views */' in line)
reader_css = "".join(css_lines[reader_platform_start:])

open('apps/web/src/app/components/studio-platform/studio-platform.component.css', 'w').write(studio_css)
open('apps/web/src/app/components/reader-platform/reader-platform.component.css', 'w').write(reader_css)
print("CSS Extraction complete")

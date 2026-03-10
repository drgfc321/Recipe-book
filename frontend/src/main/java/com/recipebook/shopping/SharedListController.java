package com.recipebook.shopping;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SharedListController {

    private final SharedListStore sharedListStore;

    public SharedListController(SharedListStore sharedListStore) {
        this.sharedListStore = sharedListStore;
    }

    @GetMapping("/share/{id}")
    public ResponseEntity<String> getSharedList(@PathVariable String id) {
        return sharedListStore.get(id)
                .map(text -> ResponseEntity.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .header("Cache-Control", "no-cache, no-store, must-revalidate")
                        .header("Pragma", "no-cache")
                        .body(renderHtml(text)))
                .orElse(ResponseEntity.notFound().build());
    }

    private String renderHtml(String text) {
        String[] lines = text.split("\n");

        // Parse into structured data
        String title = "";
        StringBuilder pageBody = new StringBuilder();
        StringBuilder shareText = new StringBuilder();
        boolean inList = false;


        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                if (inList) {
                    pageBody.append("</ul>\n");
                    inList = false;
                }
                continue;
            }

            if (title.isEmpty()) {
                // First non-empty line is the title
                title = escapeHtml(trimmed);
                pageBody.append("<h2>").append(title).append("</h2>\n");
                continue;
            }

            if (trimmed.startsWith("- ")) {
                // Item line — no "- " prefix in share text (cleaner for Notes checklist)
                String item = escapeHtml(trimmed.substring(2));
                if (!inList) {
                    pageBody.append("<ul>\n");
                    inList = true;
                }
                pageBody.append("  <li>").append(item).append("</li>\n");
                shareText.append(trimmed.substring(2)).append("\n");
            } else {
                // Category header
                if (inList) {
                    pageBody.append("</ul>\n");
                    inList = false;
                }
                String header = escapeHtml(trimmed);
                pageBody.append("<h3>").append(header).append("</h3>\n");
                // Category headers omitted from share text — only items
            }
        }
        if (inList) {
            pageBody.append("</ul>\n");
        }

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Shopping List</title>
                <style>
                  body {
                    background: #1e1e2e;
                    color: #cdd6f4;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                    margin: 0;
                    padding: 24px 16px;
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                  }
                  .content {
                    max-width: 600px;
                    width: 100%%;
                  }
                  h2 {
                    color: #cba6f7;
                    font-size: 20px;
                    margin: 0 0 16px 0;
                  }
                  h3 {
                    color: #f9e2af;
                    font-size: 15px;
                    font-weight: 700;
                    letter-spacing: 0.5px;
                    margin: 20px 0 8px 0;
                  }
                  ul {
                    list-style: disc;
                    padding-left: 24px;
                    margin: 0 0 4px 0;
                  }
                  li {
                    font-size: 16px;
                    line-height: 1.6;
                    color: #cdd6f4;
                  }
                  .share-btn {
                    background: #89b4fa;
                    color: #1e1e2e;
                    border: none;
                    border-radius: 12px;
                    padding: 14px 28px;
                    font-size: 17px;
                    font-weight: 600;
                    cursor: pointer;
                    margin-bottom: 8px;
                    width: 100%%;
                    max-width: 600px;
                    -webkit-tap-highlight-color: transparent;
                  }
                  .share-btn:active { background: #74c7ec; }
                  .copy-btn {
                    background: transparent;
                    color: #89b4fa;
                    border: 1px solid #89b4fa;
                    border-radius: 12px;
                    padding: 10px 20px;
                    font-size: 14px;
                    font-weight: 600;
                    cursor: pointer;
                    margin-bottom: 8px;
                    width: 100%%;
                    max-width: 600px;
                    -webkit-tap-highlight-color: transparent;
                  }
                  .copy-btn { transition: transform 0.12s ease, color 0.2s, border-color 0.2s; }
                  .copy-btn:active { background: rgba(137,180,250,0.1); }
                  .hint {
                    color: #6c7086;
                    font-size: 13px;
                    text-align: center;
                    margin-bottom: 20px;
                    max-width: 600px;
                  }
                  #shareText { display: none; }
                </style>
                </head>
                <body>
                <button class="share-btn" id="shareBtn" style="display:none" onclick="shareList()">Send to Notes</button>
                <button class="copy-btn" id="copyBtn" onclick="copyList()">Copy</button>
                <div class="hint">In Notes: select all &rarr; tap &#9745; to convert to checklist</div>
                <div class="content">%s</div>
                <div id="shareText">%s</div>
                <script>
                if (navigator.share) {
                  document.getElementById('shareBtn').style.display = '';
                }
                function shareList() {
                  var text = document.getElementById('shareText').innerText;
                  navigator.share({ title: 'Shopping List', text: text }).catch(function(){});
                }
                function fallbackCopy(text) {
                  var ta = document.createElement('textarea');
                  ta.value = text;
                  ta.style.position = 'fixed';
                  ta.style.left = '-9999px';
                  ta.style.top = '0';
                  ta.style.opacity = '0';
                  document.body.appendChild(ta);
                  ta.focus();
                  ta.select();
                  var ok = false;
                  try { ok = document.execCommand('copy'); } catch(e) {}
                  document.body.removeChild(ta);
                  return ok;
                }
                function showResult(btn, success) {
                  if (success) {
                    btn.textContent = 'Copied!';
                    btn.style.color = '#a6e3a1';
                    btn.style.borderColor = '#a6e3a1';
                  } else {
                    btn.textContent = 'Failed';
                    btn.style.color = '#f38ba8';
                    btn.style.borderColor = '#f38ba8';
                  }
                  setTimeout(function() { btn.textContent = 'Copy'; btn.style.color = ''; btn.style.borderColor = ''; }, 2000);
                }
                function copyList() {
                  var text = document.getElementById('shareText').innerText;
                  var btn = document.getElementById('copyBtn');
                  btn.style.transform = 'scale(0.95)';
                  setTimeout(function() { btn.style.transform = ''; }, 120);
                  if (navigator.clipboard && navigator.clipboard.writeText) {
                    navigator.clipboard.writeText(text).then(function() {
                      showResult(btn, true);
                    }).catch(function() {
                      showResult(btn, fallbackCopy(text));
                    });
                  } else {
                    showResult(btn, fallbackCopy(text));
                  }
                }
                </script>
                </body>
                </html>
                """.formatted(pageBody.toString(), escapeHtml(shareText.toString().trim()).replace("\n", "<br>"));
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}

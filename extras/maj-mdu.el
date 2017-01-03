;; maj-mdu --- mdu wrapper for Emacs

(defun maj-mdu--associated-dir ()
  (cond
   ((eq major-mode 'dired-mode)
    (dired-current-directory))
   ((buffer-file-name)
    (file-name-directory (buffer-file-name)))))

(defun maj-mdu ()
  "call mdu on the current directory"
  (interactive)
  (let ((dir (maj-mdu--associated-dir)))
    (if (null dir)
        (message "uknown directory, aborting")
      (progn
        (message "analysing dir: %s" dir)
        (with-current-buffer
            (get-buffer-create "*output*")
          (switch-to-buffer (current-buffer))
          (async-shell-command (format "mdu -s %s" dir)
                               (current-buffer)))))))

(provide 'maj-mdu)

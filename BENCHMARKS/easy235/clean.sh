grep -v '#' easy235_gt.txt > easy235_gt_comment_free.txt
sed '/^ *$/d' easy235_gt_comment_free.txt > easy235_gt_clean.txt

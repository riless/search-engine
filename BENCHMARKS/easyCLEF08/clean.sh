grep -v '#' easyCLEF08_gt.txt > easyCLEF08_gt_comment_free.txt
sed '/^ *$/d' easyCLEF08_gt_comment_free.txt > easyCLEF08_gt_clean.txt

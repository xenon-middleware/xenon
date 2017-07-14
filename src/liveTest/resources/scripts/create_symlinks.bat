mkdir filesystem-test-fixture\links
echo "Hello World" > filesystem-test-fixture\links\file0
echo "" > filesystem-test-fixture\links\file1

mklink filesystem-test-fixture\links\link0 filesystem-test-fixture\links\file0
mklink filesystem-test-fixture\links\link1 filesystem-test-fixture\links\file1
mklink filesystem-test-fixture\links\link2 filesystem-test-fixture\links\file2
mklink filesystem-test-fixture\links\link3 filesystem-test-fixture\links\link0
mklink filesystem-test-fixture\links\link4 filesystem-test-fixture\links\link2
mklink filesystem-test-fixture\links\link5 filesystem-test-fixture\links\link6
mklink filesystem-test-fixture\links\link6 filesystem-test-fixture\links\link5



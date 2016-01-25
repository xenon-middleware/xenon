if exists xenon_test exit /B


mkdir xenon_test\links
echo "Hello World" > xenon_test\links\file0
echo "" > xenon_test\links\file1

mklink xenon_test\links\link0 xenon_test\links\file0
mklink xenon_test\links\link1 xenon_test\links\file1
mklink xenon_test\links\link2 xenon_test\links\file2
mklink xenon_test\links\link3 xenon_test\links\link0
mklink xenon_test\links\link4 xenon_test\links\link2
mklink xenon_test\links\link5 xenon_test\links\link6
mklink xenon_test\links\link6 xenon_test\links\link5



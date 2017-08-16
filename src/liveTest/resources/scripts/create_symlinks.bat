mkdir filesystem-test-fixture\links
echo Hello World > filesystem-test-fixture\links\file0
type nul > filesystem-test-fixture\links\file1

mklink %CD%\filesystem-test-fixture\links\link0 %CD%\filesystem-test-fixture\links\file0
mklink %CD%\filesystem-test-fixture\links\link1 %CD%\filesystem-test-fixture\links\file1
mklink %CD%\filesystem-test-fixture\links\link2 %CD%\filesystem-test-fixture\links\file2
mklink %CD%\filesystem-test-fixture\links\link3 %CD%\filesystem-test-fixture\links\link0
mklink %CD%\filesystem-test-fixture\links\link4 %CD%\filesystem-test-fixture\links\link2
mklink %CD%\filesystem-test-fixture\links\link5 %CD%\filesystem-test-fixture\links\link6
mklink %CD%\filesystem-test-fixture\links\link6 %CD%\filesystem-test-fixture\links\link5

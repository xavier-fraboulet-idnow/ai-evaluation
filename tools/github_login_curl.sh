#curl 'http://localhost:8080/oauth2/authorization/github' \
#  -H 'Referer: http://localhost:8080/' \
#  -H 'Accept-Language: pt,en-GB;q=0.9,en-US;q=0.8,en;q=0.7' \
#  --compressed

curl 'https://github.com/login/oauth/authorize?response_type=code&client_id=2e5857e1624a7f5b787c&scope=read:user&state=PyBkhGmOZ79vLnwSIcFCEyUwYeJzg3KIa0BoHCXpQcU%3D&redirect_uri=http://localhost:8080/login/oauth2/code/github'

# -H 'Cookie: _octo=GH1.1.1285603768.1606597696; _device_id=1903f35a9ce5b216aa917780b4a8f767; cookie-preferences=analytics:accepted; user_session=2KNvo49lfd6KnvJ0BiLkiiPUvtL9kVD6lYP8SFQmz4QJz_BX; logged_in=yes; dotcom_user=pg42819; tz=Europe%2FLisbon; color_mode=%7B%22color_mode%22%3A%22auto%22%2C%22light_theme%22%3A%7B%22name%22%3A%22light%22%2C%22color_mode%22%3A%22light%22%7D%2C%22dark_theme%22%3A%7B%22name%22%3A%22dark%22%2C%22color_mode%22%3A%22dark%22%7D%7D; has_recent_activity=1; _gh_sess=0GGwCDYIcTP88Sv%2B5dCJfPEQXhOgxkoK7z1iuHGHilJsclTaRt02n0J4%2BONeSajW4QpaJfDmTsAaN2Seow1yrPtpigFqoWLgA5akAQ4lBnjlNk4mjiL%2Bl%2BKWZYF0hc37A7pcZjrSwRvZAPF0Wm6perM%2BIz87AAB1hnZ6ejlvEweEwPvrQfQbk9vP%2FQmfF3uZRgjgW7MGexWusslnwjWjeDOT2rbGl53vuxoO%2BrlOuUdAwT28e0GeOJl9gukNrrstSbSybok0qLSnegb5xguUyRCw%2BfnVnBrk3ELkG3vuuPHbK%2FVQxpimwwb8BezLxeWLUfTIdBYjlwQH4%2Bin5pwUHQ4%2FgZfz0HgW--yY%2Bn69aqR%2FV%2FF%2FCT--RuRsMjX5yS%2FRxnChsJ2IqA%3D%3D' \
curl 'http://localhost:8080/login/oauth2/code/github?code=6bdb2ac6bfc303533341&state=PyBkhGmOZ79vLnwSIcFCEyUwYeJzg3KIa0BoHCXpQcU%3D' \
  -H 'Referer: http://localhost:8080/'

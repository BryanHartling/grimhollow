# SPDX-License-Identifier: GPL-3.0-or-later
param([ValidateSet('Push','Status','Dispatch')][string]$Mode='Status')
$ErrorActionPreference='Stop'
if (!$env:GRIMHOLLOW_TOKEN) { throw 'Set GRIMHOLLOW_TOKEN in the process environment.' }
$repoRoot=Split-Path -Parent $PSScriptRoot
$gitExe='C:\Program Files\Git\cmd\git.exe'
if (!(Test-Path $gitExe)) { $gitExe='git' }
if ($Mode -eq 'Push') {
    try {
        $env:GIT_CONFIG_COUNT='1'
        $env:GIT_CONFIG_KEY_0='http.https://github.com/.extraheader'
        $env:GIT_CONFIG_VALUE_0='Authorization: Basic '+[Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes('x-access-token:'+$env:GRIMHOLLOW_TOKEN))
        & $gitExe -C $repoRoot push -u origin grimhollow
        if ($LASTEXITCODE -ne 0) { throw 'GitHub push failed (see sanitized Git output above).' }
    } finally {
        Remove-Item Env:GIT_CONFIG_COUNT,Env:GIT_CONFIG_KEY_0,Env:GIT_CONFIG_VALUE_0 -ErrorAction SilentlyContinue
    }
}
$headers=@{Authorization='Bearer '+$env:GRIMHOLLOW_TOKEN; Accept='application/vnd.github+json'; 'X-GitHub-Api-Version'='2022-11-28'}
if ($Mode -eq 'Dispatch') {
    Invoke-RestMethod -Method Post -Headers $headers -ContentType 'application/json' -Body '{"ref":"grimhollow"}' -Uri 'https://api.github.com/repos/bryanhartling/grimhollow/actions/workflows/build.yml/dispatches'
    Write-Output 'Workflow dispatch submitted.'
}
$result=Invoke-RestMethod -Headers $headers -Uri 'https://api.github.com/repos/bryanhartling/grimhollow/actions/runs?branch=grimhollow&per_page=3'
Write-Output ("Workflow runs found: " + $result.total_count)
if ($result.total_count -eq 0) {
    $repository=Invoke-RestMethod -Headers $headers -Uri 'https://api.github.com/repos/bryanhartling/grimhollow'
    $repository | Select-Object full_name,default_branch,fork
    $workflowList=Invoke-RestMethod -Headers $headers -Uri 'https://api.github.com/repos/bryanhartling/grimhollow/actions/workflows'
    $workflowList.workflows | Select-Object id,name,state,path
}
foreach($run in $result.workflow_runs) {
    $run | Select-Object id,head_sha,status,conclusion,html_url
    $jobs=Invoke-RestMethod -Headers $headers -Uri $run.jobs_url
    $jobs.jobs | Select-Object name,status,conclusion,html_url
    if ($run -eq $result.workflow_runs[0]) {
        foreach ($job in $jobs.jobs) {
            Write-Output ("Steps for " + $job.name)
            $job.steps | Where-Object { $_.name -notlike 'Post *' } | Select-Object name,status,conclusion
        }
    }
}

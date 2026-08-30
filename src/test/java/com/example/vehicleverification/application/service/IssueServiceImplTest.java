package com.example.vehicleverification.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.vehicleverification.application.dto.issue.IssueCreateRequest;
import com.example.vehicleverification.application.dto.issue.IssueCreateResponse;
import com.example.vehicleverification.application.dto.issue.IssueDetailResponse;
import com.example.vehicleverification.application.dto.issue.IssueDto;
import com.example.vehicleverification.application.dto.issue.IssueUpdateRequest;
import com.example.vehicleverification.application.dto.issue.IssueUpdateResponse;
import com.example.vehicleverification.domain.entity.Issue;
import com.example.vehicleverification.domain.entity.Model;
import com.example.vehicleverification.domain.entity.ReviewMeeting;
import com.example.vehicleverification.domain.entity.User;
import com.example.vehicleverification.domain.exception.ResourceNotFoundException;
import com.example.vehicleverification.domain.repository.IssueRepository;
import com.example.vehicleverification.domain.repository.ReviewMeetingRepository;
import com.example.vehicleverification.domain.repository.UserRepository;
import jakarta.persistence.OptimisticLockException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class IssueServiceImplTest {
    @Mock
    private IssueRepository issueRepository;

    @Mock
    private ReviewMeetingRepository reviewMeetingRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private IssueServiceImpl issueService;

    private Issue createDummyIssue(Long id, String status, String answer, Long version) {
        Model model = new Model("MC0" + id, "モデル" + id, 2026, "テストECU", "エンジン", "AMD", "詳細", "仕向け地", "HEV");
        model.setId(id);

        User reporter = new User("reporter" + id, "pass" + id, "報告者" + id, "USER", "開発部");
        reporter.setId(id);

        ReviewMeeting reviewMeeting =
                new ReviewMeeting(model, "定例レビュー" + id, LocalDate.of(2026, 1, 1), "予定", reporter, "備考", "EVT" + id);
        reviewMeeting.setId(id);

        Issue issue = new Issue(reviewMeeting, "指摘内容" + id, reporter, status);
        issue.setId(id);
        issue.setAnswer(answer);
        issue.setVersion(version);
        return issue;
    }

    private User createDummyUser(Long id, String displayName) {
        User user = new User("user" + id, "pass", displayName, "USER", "開発部");
        user.setId(id);
        return user;
    }

    @Test
    void getIssueById_存在するIDを指定した場合_DetailResponseを返す() {
        Issue issue = createDummyIssue(1L, "未対応", "初期回答", 0L);
        given(issueRepository.findById(issue.getId())).willReturn(Optional.of(issue));

        IssueDetailResponse response = issueService.getIssueById(issue.getId());

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(issue.getId());
        assertThat(response.getStatus()).isEqualTo(issue.getStatus());
        assertThat(response.getReviewMeetingId())
                .isEqualTo(issue.getReviewMeeting().getId());
        assertThat(response.getReporterId()).isEqualTo(issue.getReporter().getId());
        // answerer未設定のためnullが返る想定
        assertThat(response.getAnswererId()).isNull();
    }

    @Test
    void getIssueById_存在しないIDを指定した場合_例外をスローする() {
        Long nonExistentId = 999L;
        given(issueRepository.findById(nonExistentId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> issueService.getIssueById(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getIssueAll_reviewMeetingIdとstatusが両方nullの場合_全件返す() {
        Issue issue1 = createDummyIssue(1L, "未対応", "初期回答", 0L);
        Issue issue2 = createDummyIssue(2L, "対応中", "回答中", 0L);
        given(issueRepository.findAll()).willReturn(List.of(issue1, issue2));

        List<IssueDto> issues = issueService.getIssueAll(null, null);

        assertThat(issues).hasSize(2);
        verify(issueRepository).findAll();
    }

    @Test
    void createIssue_正常系_CreateResponseを返す() {
        // Arrange
        Issue issue = createDummyIssue(1L, null, null, 0L);
        ReviewMeeting reviewMeeting = issue.getReviewMeeting();
        User reporter = issue.getReporter();

        IssueCreateRequest request = new IssueCreateRequest();
        request.setReviewMeetingId(reviewMeeting.getId());
        // saveはモックのためレスポンスはダミーissueの値になる。ダミーのcontentに揃える
        request.setContent(issue.getContent());
        request.setReporterId(reporter.getId());

        given(reviewMeetingRepository.findById(reviewMeeting.getId())).willReturn(Optional.of(reviewMeeting));
        given(userRepository.findById(reporter.getId())).willReturn(Optional.of(reporter));
        // Issueはサービスが内部で new Issue(...) するので save(any(Issue.class)) をモックする
        given(issueRepository.save(any(Issue.class))).willReturn(issue);

        // Act
        IssueCreateResponse response = issueService.createIssue(request);

        // Assert
        assertThat(response.getContent()).isEqualTo(request.getContent());
        assertThat(response.getReviewMeetingId()).isEqualTo(reviewMeeting.getId());
        assertThat(response.getReporterId()).isEqualTo(reporter.getId());
    }

    @Test
    void createIssue_存在しないreviewMeetingIdを指定した場合_例外をスローする() {
        Long nonExistentReviewMeetingId = 999L;
        IssueCreateRequest request = new IssueCreateRequest();
        request.setReviewMeetingId(nonExistentReviewMeetingId);
        request.setContent("指摘内容");
        request.setReporterId(1L);

        given(reviewMeetingRepository.findById(nonExistentReviewMeetingId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> issueService.createIssue(request)).isInstanceOf(ResourceNotFoundException.class);
        verify(userRepository, never()).findById(any());
    }

    @Test
    void updateIssue_送られた項目のみ更新し未指定項目は据え置く() {
        // Arrange
        // 初期状態
        Issue issue = createDummyIssue(1L, "未対応", "初期回答", 0L);

        // statusのみ更新
        IssueUpdateRequest request = new IssueUpdateRequest();
        request.setStatus("完了");
        request.setVersion(0L);

        given(issueRepository.findById(issue.getId())).willReturn(Optional.of(issue));
        given(issueRepository.saveAndFlush(any(Issue.class))).willReturn(issue);

        // Act
        IssueUpdateResponse response = issueService.updateIssue(issue.getId(), request);

        // Assert
        assertThat(response.getStatus()).isEqualTo("完了");
        assertThat(response.getAnswer()).isEqualTo("初期回答");
        assertThat(response.getResolvedAt()).isNull();
    }

    @Test
    void updateIssue_answerIdを指定した場合_answererがセットされる() {
        // Arrange
        Issue issue = createDummyIssue(1L, "未対応", "初期回答", 0L);
        User answerer = createDummyUser(2L, "回答者");

        IssueUpdateRequest request = new IssueUpdateRequest();
        request.setAnswer("更新回答");
        request.setAnswererId(answerer.getId());
        request.setStatus("完了");
        request.setVersion(0L);

        given(issueRepository.findById(issue.getId())).willReturn(Optional.of(issue));
        given(userRepository.findById(answerer.getId())).willReturn(Optional.of(answerer));
        given(issueRepository.saveAndFlush(any(Issue.class))).willReturn(issue);

        // Act
        IssueUpdateResponse response = issueService.updateIssue(issue.getId(), request);

        // Assert
        assertThat(response.getAnswer()).isEqualTo("更新回答");
        assertThat(response.getAnswererId()).isEqualTo(answerer.getId());
        assertThat(response.getAnswererName()).isEqualTo("回答者");
    }

    @Test
    void updateIssue_存在しないanswererIdを指定した場合_例外をスローする() {
        // Arrange
        Issue issue = createDummyIssue(1L, "未対応", "初期回答", 0L);
        Long nonExistentAnswererId = 999L;

        IssueUpdateRequest request = new IssueUpdateRequest();
        request.setAnswererId(nonExistentAnswererId);
        request.setVersion(0L);

        given(issueRepository.findById(issue.getId())).willReturn(Optional.of(issue));
        given(userRepository.findById(nonExistentAnswererId)).willReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> issueService.updateIssue(issue.getId(), request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(issueRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateIssue_version不一致の場合_楽観ロック例外をスローする() {
        // Arrange
        Issue issue = createDummyIssue(1L, "未対応", "初期回答", 0L);

        IssueUpdateRequest request = new IssueUpdateRequest();
        request.setStatus("完了");
        request.setVersion(1L); // 現在のバージョンと異なる値を設定

        given(issueRepository.findById(issue.getId())).willReturn(Optional.of(issue));

        // Act & Assert
        assertThatThrownBy(() -> issueService.updateIssue(issue.getId(), request))
                .isInstanceOf(OptimisticLockException.class);
        // 競合検知時は保存に進まない
        verify(issueRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateIssue_存在しないIDを指定した場合_例外をスローする() {
        Long nonExistentId = 999L;
        IssueUpdateRequest request = new IssueUpdateRequest();
        request.setStatus("完了");
        request.setVersion(0L);

        given(issueRepository.findById(nonExistentId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> issueService.updateIssue(nonExistentId, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteIssue_存在するIDを指定した場合_正常に削除される() {
        Issue issue = createDummyIssue(1L, "未対応", "初期回答", 0L);
        given(issueRepository.findById(issue.getId())).willReturn(Optional.of(issue));

        issueService.deleteIssue(issue.getId());

        verify(issueRepository).delete(issue);
    }

    @Test
    void deleteIssue_存在しないIDを指定した場合_例外をスローする() {
        Long nonExistentId = 999L;
        given(issueRepository.findById(nonExistentId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> issueService.deleteIssue(nonExistentId)).isInstanceOf(ResourceNotFoundException.class);
        verify(issueRepository, never()).delete(any());
    }
}

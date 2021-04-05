package service.impl;

import dto.impl.AccountDto;
import model.Account;
import repository.hibernate.impl.AccountRepositoryImpl;
import service.Service;

import java.util.List;

public class AccountServiceImpl implements Service<Account> {

    private final AccountDto accountDto = new AccountDto();
    private final AccountRepositoryImpl repository = new AccountRepositoryImpl();

    @Override
    public Account post(Account entity) {
        return repository.save(entity);
    }

    @Override
    public Account put(Account entity) {
        return repository.update(entity);
    }

    @Override
    public void delete(Long id) {
        repository.delete(id);
    }

    @Override
    public Account get(Long id) {
        Account account =  repository.find(id);
        return accountDto.get(account);
    }

    @Override
    public List<Account> getAll() {
        List<Account> accounts = repository.findAll();
        return accountDto.getAll(accounts);
    }

    @Override
    public String getJson(List<Account> list) {
        return accountDto.getJson(list);
    }

}
